package it.devddk.hackernewsclient.data.repository.item

import android.util.LruCache
import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.data.networking.model.ItemResponse
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalStateException
import java.time.LocalDateTime

class ItemRepositoryImpl : ItemRepository, KoinComponent {

    private val items: DatabaseReference by inject(named("item"))
    private val itemDao: ItemEntityDao by inject()
    private val collectionDao: ItemCollectionEntityDao by inject()
    private val connectivity: Connectivity by inject()


    private val cache = LruCache<ItemId, Item>(500)

    override suspend fun getItemById(itemId: Int, forceRefresh: Boolean): Result<Item> {
        if (forceRefresh) {
            return fetchItemOnlineOrOffline(itemId).onSuccess { item ->
                cache.put(itemId, item)
            }
        }
        val cacheItem = cache.get(itemId)
        if (cacheItem != null) {
            return Result.success(cacheItem)
        }
        return fetchItemOnlineOrOffline(itemId).onSuccess { item ->
            cache.put(itemId, item)
        }
    }

    private suspend fun fetchItemOnlineOrOffline(itemId: Int): Result<Item> {
        // Supervisor scope: Don't kill parent coroutine in case of a failure
        return supervisorScope {
            cache.get(itemId)
            // Run on IO thread pool
            withContext(Dispatchers.IO) {
                // Start asking for collections in local database
                val collectionsDeferred = async {
                    return@async collectionDao.getAllCollectionsForItem(itemId)
                }

                val itemResult = if (connectivity.hasNetworkAccess()) {
                    runCatching {
                        // Start asking for item on HN api
                        val hnResult = items.child(itemId.toString()).get().await()
                        val data = hnResult.getValue(ItemResponse::class.java)
                        checkNotNull(data?.mapToDomainModel()) { "Received null item" }
                    }.recoverCatching { exception ->
                        // Skip offline check if canceled
                        if (exception is CancellationException) {
                            throw exception
                        }
                        fetchItemOffline(itemId)
                    }
                } else {
                    runCatching {
                        fetchItemOffline(itemId)
                    }
                }

                itemResult.mapCatching { item ->
                    // Fetch collections from local database, if Item fetching didn't not fail
                    val collections = try {
                        collectionsDeferred.await()
                            .mapNotNull(ItemCollectionEntity::mapToDomainModel).toSet()
                    } catch (e: Exception) {
                        Timber.w("Failed to retrieve collections for item $itemId")
                        emptySet()
                    }
                    item.copy(collections = collections.associateBy { entry -> entry.collection })
                }.onFailure { e ->
                    Timber.e("Item $itemId could not be retrieved. Cause ${e.message}")
                }
            }
        }
    }


    private suspend fun fetchItemOffline(itemId: Int): Item {
        return checkNotNull(itemDao.getItem(itemId)) { " Item $itemId unavailable online and offline" }
            .mapToDomainModel()
    }

    override suspend fun saveItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDao.insertItem(item.toItemEntity())
        }

    }

    override suspend fun addItemToCollection(
        id: ItemId,
        collection: UserDefinedItemCollection,
    ): Result<Unit> {
        return runCatching {
            collectionDao.addItemToCollection(ItemCollectionEntity(id,
                collection::class.simpleName!!, LocalDateTime.now())).let { rowsAdded ->
                if (rowsAdded < 0) {
                    throw IllegalStateException("Failed query")
                }
                cache.remove(id)
            }
        }

    }

    override suspend fun removeItemFromCollection(
        id: ItemId,
        collection: UserDefinedItemCollection,
    ): Result<Unit> {
        return runCatching {
            collectionDao.removeItemFromCollection(id,
                collection::class.simpleName!!).let { rowsDeleted ->
                if (rowsDeleted != 1) {
                    throw IllegalStateException("Failed query")
                }
                cache.remove(id)
            }
            cache.remove(id)
        }

    }

    override suspend fun getCollectionsOfItem(id: ItemId): Result<Set<UserDefinedItemCollection>> {
        return runCatching {
            collectionDao.getAllCollectionsForItem(id).map {
                UserDefinedItemCollection.valueOf(it.collection)!!
            }.toSet()
        }
    }

    override suspend fun getAllItemsForCollection(collection: UserDefinedItemCollection): Result<List<ItemId>> {
        return runCatching {
            collectionDao.getAllIdsForCollection(collection::class.simpleName!!).map {
                it.id
            }
        }
    }

    override suspend fun invalidateCache() {
        cache.evictAll()
    }
}
