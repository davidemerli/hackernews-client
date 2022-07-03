package it.devddk.hackernewsclient.data.repository.item

import android.util.LruCache
import androidx.room.withTransaction
import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.api.AlgoliaSearchApi
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.dao.SaveItemDao
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.data.networking.base.asResult
import it.devddk.hackernewsclient.data.networking.model.ItemResponse
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.ItemTree
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
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
    private val db: LocalDatabase by inject()
    private val saveItemDao = db.saveItemDao()
    private val collectionDao = db.itemCollectionDao()
    private val algoliaApi: AlgoliaSearchApi by inject()
    private val connectivity: Connectivity by inject()


    private val cache: LruCache<ItemId, Item> by inject()

    override suspend fun getItemById(
        itemId: Int,
        fetchStrategy: ItemRepository.FetchMode,
    ): Result<Item> {

        val currFetchStrategy = if (connectivity.hasNetworkAccess()) {
            fetchStrategy
        } else {
            fetchStrategy.onConnectivityAbsent()
        }

        val fetchOrder = currFetchStrategy.fetchOrder
        if (fetchOrder.isEmpty()) {
            return Result.failure(exception = IllegalArgumentException("Empty fetch strategy"))
        }

        var result: Result<Item> = runCatching {
            throw IllegalFetchStrategy()
        }

        val problems = mutableListOf<Throwable>()

        for (source in fetchOrder) {

            result = result.recoverCatching {
                when (it) {
                    is IllegalFetchStrategy -> {
                        // Not relevant exception
                    }
                    is CancellationException -> {
                        // Rethrow to continue
                        throw CancellationException()
                    }
                    else -> problems.add(it)
                }
                //Use the fetcher
                val fetcher = sources[source]!!
                fetcher(itemId)
            }
        }

        return result.onFailure {
            if (it !is CancellationException) {
                Timber.e("Failed to retrieve item $itemId:\n${
                    problems.mapIndexed { i, t ->
                        "Attempt #" + (i + 1) + " " + fetchOrder[i]::class.simpleName + ": " + t.message + " \n"
                    }
                }")
            }

        }
    }


    override suspend fun saveItem(item: ItemId): Result<Unit> {
        Timber.d("Getting $item from algolia to be saved")
        return algoliaApi.getItemById(item).asResult().mapCatching { algolia ->
            val rootItem = if (algolia.storyId != null) {
                Timber.d("Algolia returned! Item is not root, requesting ${algolia.storyId}")
                algoliaApi.getItemById(algolia.storyId).asResult().getOrThrow()
            } else {
                Timber.d("Algolia returned! Item is root")
                algolia
            }
            val linearizedCommentTree = rootItem.mapToDomainModel().dfsWalkComments().map {
                it.toItemEntity()
            }
            db.withTransaction {
                val savedItems = itemDao.insertItems(linearizedCommentTree)
                Timber.d("Item id $item: Saved ${savedItems.size}/${linearizedCommentTree.size} sub items")
                if (savedItems.isEmpty()) {
                    throw IllegalStateException("Failed to save sub items in database")
                }
            }
        }.onFailure {
            Timber.e("Failed to persist item. \n${it.stackTraceToString()}")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun addItemToCollection(
        id: ItemId,
        collection: UserDefinedItemCollection,
    ): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val needsSaveItem = db.withTransaction {
                    val collectionName = collection::class.simpleName!!
                    val rowsAdded = collectionDao.addItemToCollection(ItemCollectionEntity(id,
                        collection, LocalDateTime.now()))
                    Timber.d("Added item $id to $collectionName")
                    if (rowsAdded < 0) {
                        throw IllegalStateException("Failed query")
                    }
                    collection.saveWholeItem && saveItemDao.needsToBeSaved(collection,
                        id,
                        ItemRepository.MIN_TIME_FOR_REFRESH_SECS)

                }
                cache.remove(id)
                Timber.d("Added item $id needs to be saved? $needsSaveItem")
                if (needsSaveItem) {
                    saveItem(id)
                }
            }
            Timber.d("Saving transaction finished")
        }
    }


    override suspend fun removeItemFromCollection(
        id: ItemId,
        collection: UserDefinedItemCollection,
    ): Result<Unit> {
        return runCatching {
            val rowsDeleted = collectionDao.removeItemFromCollection(id,
                collection)
            if (rowsDeleted != 1) {
                throw IllegalStateException("Failed query")
            }
            Timber.d("Removing $id from ${collection::class.java}")
            cache.remove(id)
        }
    }

    override suspend fun getCollectionsOfItem(id: ItemId): Result<Set<UserDefinedItemCollection>> {
        return runCatching {
            collectionDao.getAllCollectionsForItem(id).map {
                it.collection
            }.toSet()
        }
    }

    override suspend fun getAllItemsForCollection(collection: UserDefinedItemCollection): Result<List<ItemId>> {
        return runCatching {
            collectionDao.getAllIdsForCollection(collection).map {
                it.id
            }
        }
    }

    override suspend fun invalidateCache() {
        cache.evictAll()
    }

    private val fetchFromCache: suspend (Int) -> Item = { id: Int ->
        val item = cache.get(id)
        item ?: throw ItemNotFoundException(id, ItemRepository.Cache)
    }

    private val fetchFromOnline: suspend (Int) -> Item = { itemId: Int ->
        // Start asking for item on HN api
        val hnResult = items.child(itemId.toString()).get().await()
        val data = hnResult.getValue(ItemResponse::class.java)
        Timber.d("hnResult: $hnResult - $data")
        val item =
            checkNotNull(data?.mapToDomainModel()
                ?.copy(downloaded = LocalDateTime.now())) { "Item $itemId not available online" }
        cache.put(itemId, item)
        item
    }

    private val fetchFromOffline: suspend (Int) -> Item = { itemId: Int ->
        val item =
            checkNotNull(itemDao.getItem(itemId)) { "Item $itemId not available offline" }.mapToDomainModel()
        cache.put(itemId, item)
        item
    }

    override suspend fun getCommentTree(id: ItemId): Result<ItemTree> {
        return algoliaApi.getItemById(id).asResult().mapCatching {
            it.mapToDomainModel()
        }
    }


    private val sources = mapOf(
        ItemRepository.Cache to fetchFromCache,
        ItemRepository.Online to fetchFromOnline,
        ItemRepository.Offline to fetchFromOffline
    )

    private class ItemNotFoundException(
        val id: ItemId,
        val source: ItemRepository.ItemSources,
        message: String = "Unable to fetch item $id from ${source::class.java.simpleName}",
    ) : Exception(message)

    private class IllegalFetchStrategy : Exception("Illegal fetch strategy")


}
