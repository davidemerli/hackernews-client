package it.devddk.hackernewsclient.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import it.devddk.hackernewsclient.data.database.entities.ItemEntity
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime

@Dao
interface ItemEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(entity: ItemEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(entities: List<ItemEntity>) : List<Long>

    @Query("SELECT * FROM items WHERE :itemId = items.id LIMIT 1")
    suspend fun getItem(itemId: ItemId): ItemEntity?

    @Query("SELECT saved " +
            "FROM items WHERE :itemId = items.id LIMIT 1")
    suspend fun getSavedTime(itemId: ItemId): LocalDateTime?

    @Query("SELECT downloaded " +
            "FROM items WHERE :itemId = items.id LIMIT 1")
    suspend fun getDownloadedTime(itemId: ItemId): LocalDateTime?

    @Query("SELECT htmlPage FROM items WHERE :itemId = items.id LIMIT 1")
    suspend fun getHtml(itemId: ItemId): String?

    @Transaction
    suspend fun _increaseRefCount(itemId: ItemId) {

    }

    @Transaction
    suspend fun _decreaseRefCount(itemId: ItemId) {

    }

}