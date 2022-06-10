package it.devddk.hackernewsclient.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import it.devddk.hackernewsclient.data.database.entities.ItemEntity

@Database(entities = [ItemEntity::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
}