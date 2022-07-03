package it.devddk.hackernewsclient.data.database.entities

import androidx.room.Entity
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.domain.model.collection.ItemCollectionEntry
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import java.time.LocalDateTime

@Entity(tableName = "items_collection", primaryKeys = ["id", "collection"])
data class ItemCollectionEntity(
    val id: Int,
    val collection: UserDefinedItemCollection,
    val timeAdded: LocalDateTime
) : DomainMapper<ItemCollectionEntry?> {
    override fun mapToDomainModel(): ItemCollectionEntry? {
        return ItemCollectionEntry(collection, timeAdded)
    }
}