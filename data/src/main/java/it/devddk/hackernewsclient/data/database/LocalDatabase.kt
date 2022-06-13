package it.devddk.hackernewsclient.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.entities.ItemEntity

@Database(entities = [ItemEntity::class], version = 1)
@TypeConverters(ItemConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun itemEntityDao() : ItemEntityDao

    abstract fun itemCollectionDao() : ItemCollectionEntityDao
}