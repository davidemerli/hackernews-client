package it.devddk.hackernewsclient.data.repository.item

import android.util.LruCache
import androidx.room.withTransaction
import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.api.AlgoliaSearchApi
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.data.networking.base.asResult
import it.devddk.hackernewsclient.data.networking.model.ItemResponse
import it.devddk.hackernewsclient.data.networking.utils.getBody
import it.devddk.hackernewsclient.data.networking.utils.getUrl
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.flatMap
import it.devddk.hackernewsclient.domain.model.items.ItemTree
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
    private val okHttpClient: OkHttpClient by inject()

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

        // Independently from the source interrogate the database to get collections and storyId
        result = result.mapCatching { item ->
            try {
                val collections = collectionDao.getAllCollectionsForItem(itemId)
                    .map { it.mapToDomainModel() }.associateBy {
                        it.collection
                    }
                val storyId = item.storyId ?: itemDao.getParentStoryId(itemId)
                item.copy(collections = collections, storyId = storyId)
            } catch (e: Exception) {
                item
            }
        }.mapCatching { item ->
            // Save item in cache
            cache.put(itemId, item)
            item
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

    /**
     * Adds an Item to a [UserDefinedItemCollection]. In some cases all its children to be available offline
     *
     * As the first thing the item will be inside the [ItemCollectionEntity] in order to be marked as a
     * member of the collection. After that it will check if the [UserDefinedItemCollection.saveWholeItem] is
     * true. In that case it will save the item offline in [Item] table, together with all his children.
     *
     *
     * Don't use this method to just refresh the saved Item without adding it to a new collection. For that
     * case it is sufficient to invoke the method [saveItem]
     *
     * @param id The id to add to a [UserDefinedItemCollection]
     * @param collection The collection in which the item should be added
     *
     * @return A result item that is successful if the operation is successful
     */
    override suspend fun addItemToCollection(
        id: ItemId,
        collection: UserDefinedItemCollection,
    ): Result<Unit> {
        return runCatching {
            // Inside the IO thread
            withContext(Dispatchers.IO) {
                val itemEntityToPut = ItemCollectionEntity(id, collection, LocalDateTime.now())
                db.withTransaction {
                    val collectionName = collection::class.simpleName!!
                    val oldCollection = collectionDao.getItemCollectionEntity(id, collection)
                    if (oldCollection != null && !collection.allowReinsertion) {
                        throw IllegalStateException("Item $id already present in ${collection::class.java}, re-insertion not allowed!")
                    }
                    val rowsAdded = collectionDao.addItemToCollection(itemEntityToPut)
                    Timber.d("Added item $id to $collectionName")
                    if (rowsAdded < 0) {
                        throw IllegalStateException("Failed query")
                    }
                }

                cache.get(id)?.let { item ->
                    cache.put(id,
                        item.copy(collections = item.collections.plus(Pair(collection,
                            itemEntityToPut.mapToDomainModel()))))
                }

                val needsSaveItem =
                    collection.saveWholeItem && saveItemDao.needsToBeSaved(collection,
                        id,
                        ItemRepository.MIN_TIME_FOR_REFRESH_SECS)

                if (needsSaveItem) {
                    saveItem(id)
                }

                Timber.d("Added item $id. Operations: Save item? $needsSaveItem.  Current refCount: ${
                    saveItemDao.computeRefCount(id)
                }")
            }
            Timber.d("Saving transaction finished")
        }
    }


    override suspend fun saveItem(item: ItemId): Result<Unit> {
        Timber.d("Getting $item from algolia to be saved")
        return withContext(Dispatchers.IO) {
            return@withContext algoliaApi.getItemById(item).asResult().mapCatching { algolia ->
                val rootItemResponse = if (algolia.storyId != null) {
                    Timber.d("Algolia returned! Item is not root, requesting ${algolia.storyId}")
                    algoliaApi.getItemById(algolia.storyId).asResult().getOrThrow()
                } else {
                    Timber.d("Algolia returned! Item is root")
                    algolia
                }

                val root = rootItemResponse.mapToDomainModel()
                val linearizedCommentTree = root.dfsWalkComments().map { item ->
                    item.toItemEntity()
                }

                db.withTransaction {
                    val savedItems = itemDao.insertItems(linearizedCommentTree)
                    Timber.d("Item id $item: Saved ${savedItems.size}/${linearizedCommentTree.size} sub items.")
                    if (savedItems.isEmpty()) {
                        throw IllegalStateException("Failed to save sub items in database")
                    }
                }
                /*
                if (root.item.url != null) {
                    launch {
                        Timber.d("Downloading $item url: ${root.item.url}")
                        saveUrl(url = root.item.url!!).mapCatching { html ->
                            itemDao.saveHtml(item, html)
                        }.fold(onSuccess = {
                            Timber.i("Saved $item url: ${root.item.url} with success")
                        }, onFailure = {
                            Timber.i("Failed to save $item url: ${root.item.url}. ${it::class.java.simpleName} ${it.message}")
                        })
                    }
                }*/


            }.onFailure {
                Timber.e("Failed to persist item. \n${it.stackTraceToString()}")
            }
        }
    }

    private suspend fun saveUrl(url: String): Result<String> {
        return runCatching {
            val request = Request.Builder().url(url).build()
            getBody(okHttpClient, request)
        }
    }

    override suspend fun removeItemFromCollection(
        id: ItemId,
        collection: UserDefinedItemCollection,
    ): Result<Unit> {
        return runCatching {
            db.withTransaction {
                val rowsDeleted = collectionDao.removeItemFromCollection(id, collection)
                if (rowsDeleted != 1) {
                    throw IllegalStateException("Failed query")
                }
                val refCount = saveItemDao.computeRefCount(id)
                Timber.d("Removing item $id from ${collection::class.java.simpleName}. Current refCount $refCount")
                if (refCount == 0) {
                    Timber.d("Removing item $id from database")
                    itemDao.deleteItem(id)
                }
            }
            cache.remove(id)
            cache.get(id)?.let { item ->
                cache.put(id,
                    item.copy(collections = item.collections.minus(collection)))
            }

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

        val item = withContext(Dispatchers.IO) {
            checkNotNull(data?.mapToDomainModel()
                ?.copy(downloaded = LocalDateTime.now())) { "Item $itemId not available online" }
        }
        item
    }

    private val fetchFromOffline: suspend (Int) -> Item = { itemId: Int ->
        val item =
            checkNotNull(itemDao.getItem(itemId)) { "Item $itemId not available offline" }.mapToDomainModel()
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
