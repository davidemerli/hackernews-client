package it.devddk.hackernewsclient.data.repository.item

import com.google.firebase.database.DatabaseReference
import it.devddk.hackernewsclient.data.common.utils.ResponseConversionException
import it.devddk.hackernewsclient.data.networking.model.ItemResponse
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception

class ItemRepositoryImpl() : ItemRepository, KoinComponent {

    val dbRef: DatabaseReference by inject()

    override suspend fun getItemById(itemId: Int): Result<Item> {
        return runCatching {
            val snap = dbRef.child("item").get().await()
            val data = snap.getValue(ItemResponse::class.java)
            checkNotNull(data?.mapToDomainModel()) { "Received null item" }
        }
    }
}