package it.devddk.hackernewsclient.data.repository.item

import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.data.networking.model.ItemResponse
import it.devddk.hackernewsclient.domain.model.items.Item
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

class ItemRepositoryImpl : ItemRepository, KoinComponent {

    private val items: DatabaseReference by inject(named("item"))
    private val itemDao: ItemEntityDao by inject()
    private val collectionDao: ItemCollectionEntityDao by inject()
    private val connectivity: Connectivity by inject()

    override suspend fun getItemById(itemId: Int): Result<Item> {
        // Supervisor scope: Don't kill parent coroutine in case of a failure
        return supervisorScope {
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
                    item.copy(collections = collections)
                }.onFailure { e ->
                    Timber.e("Item $itemId could not be retrieved. Cause ${e.message}")
                }
            }
        }
    }


    private suspend fun fetchItemOffline(itemId: Int) : Item {
        return checkNotNull(itemDao.getItem(itemId)) { " Item $itemId unavailable online and offline" }
            .mapToDomainModel()
    }

    override suspend fun saveItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDao.insertItem(item.toItemEntity())
        }

    }
}
