package it.devddk.hackernewsclient.data.database.entities

import androidx.room.Entity
import it.devddk.hackernewsclient.data.networking.base.DomainMapper
import it.devddk.hackernewsclient.domain.model.utils.SavedItemCollection

@Entity(tableName = "items_collection", primaryKeys = ["id", "collection"])
data class ItemCollectionEntity(
    val id: Int,
    val collection: String
) : DomainMapper<SavedItemCollection?> {
    override fun mapToDomainModel(): SavedItemCollection? {
        return SavedItemCollection.valueOf(collection)
    }
}
