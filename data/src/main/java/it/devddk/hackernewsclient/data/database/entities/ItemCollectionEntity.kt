package it.devddk.hackernewsclient.data.database.entities

import androidx.room.Entity

@Entity(tableName = "items_collection", primaryKeys = ["id", "collection"])
data class ItemCollectionEntity(
    val id: Int,
    val collection: String
)
