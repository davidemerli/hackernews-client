package it.devddk.hackernewsclient.data.repository.item

import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.data.networking.model.ItemResponse
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber
import java.lang.Exception

class ItemRepositoryImpl : ItemRepository, KoinComponent {

    private val items: DatabaseReference by inject(named("item"))
    private val itemDao: ItemEntityDao by inject()

    override suspend fun getItemById(itemId: Int): Result<Item> {
        return try {
            val snap = items.child(itemId.toString()).get().await()
            val data = snap.getValue(ItemResponse::class.java)
            val result = checkNotNull(data?.mapToDomainModel()) { "Received null item" }
            Result.success(result)
        } catch (e: Exception) {
            Timber.e("Item $itemId could not be retrieved. Cause ${e.message}")
            Timber.d(e)
            Result.failure(e)
        }
    }

    override suspend fun saveItem(item: Item) {
        val copiedItem : Item
        if(item.url != null && item.htmlPage == null) {
            copiedItem = item.copy(htmlPage = item.htmlPage)
        }
        itemDao.insertItem(item.toItemEntity())
    }
}
