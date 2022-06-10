package it.devddk.hackernewsclient.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items", primaryKeys = ["source","id"])
data class ItemEntity(
    val source: String,
    val id: Int,
)
