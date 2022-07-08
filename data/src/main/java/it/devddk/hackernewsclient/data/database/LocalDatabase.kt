package it.devddk.hackernewsclient.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.dao.SaveItemDao
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.ItemEntity

@Database(entities = [ItemEntity::class, ItemCollectionEntity::class], version = 5)
@TypeConverters(ItemConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun itemEntityDao() : ItemEntityDao

    abstract fun itemCollectionDao() : ItemCollectionEntityDao

    abstract fun saveItemDao() : SaveItemDao
}