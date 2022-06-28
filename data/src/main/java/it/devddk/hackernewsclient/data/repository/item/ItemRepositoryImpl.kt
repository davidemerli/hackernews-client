package it.devddk.hackernewsclient.data.repository.item

import android.util.LruCache
import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.dao.SaveItemDao
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.data.networking.model.ItemResponse
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
    private val saveItemDao: SaveItemDao by inject()
    private val collectionDao: ItemCollectionEntityDao by inject()
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

    private val fetchFromCache: suspend (Int) -> Item = { id: Int ->
        val item = cache.get(id)
        item ?: throw ItemNotFoundException(id, ItemRepository.Cache)
    }

    private val fetchFromOnline: suspend (Int) -> Item = { itemId: Int ->
        // Start asking for item on HN api
        val hnResult = items.child(itemId.toString()).get().await()
        val data = hnResult.getValue(ItemResponse::class.java)
        Timber.d("hnResult: $hnResult - $data")
        val item = checkNotNull(data?.mapToDomainModel()) { "Item $itemId not available online" }
        cache.put(itemId, item)
        item
    }

    private val fetchFromOffline: suspend (Int) -> Item = { itemId: Int ->
        val item =
            checkNotNull(itemDao.getItem(itemId)) { "Item $itemId not available offline" }.mapToDomainModel()
        cache.put(itemId, item)
        item
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
