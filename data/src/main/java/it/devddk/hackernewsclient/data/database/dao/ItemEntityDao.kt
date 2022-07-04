package it.devddk.hackernewsclient.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import it.devddk.hackernewsclient.data.database.entities.ItemEntity
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import java.time.LocalDateTime

@Dao
abstract class ItemEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItem(entity: ItemEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertItems(entities: List<ItemEntity>) : List<Long>

    @Query("SELECT * FROM items WHERE :itemId = items.id LIMIT 1")
    abstract suspend fun getItem(itemId: ItemId): ItemEntity?

    @Query("SELECT saved " +
            "FROM items WHERE :itemId = items.id LIMIT 1")
    abstract suspend fun getSavedTime(itemId: ItemId): LocalDateTime?

    @Query("SELECT downloaded " +
            "FROM items WHERE :itemId = items.id LIMIT 1")
    abstract suspend fun getDownloadedTime(itemId: ItemId): LocalDateTime?

    @Query("SELECT htmlPage FROM items WHERE :itemId = items.id LIMIT 1")
    abstract suspend fun getHtml(itemId: ItemId): String?

    @Query("DELETE FROM items WHERE :itemId == items.id")
    abstract suspend fun deleteItem(itemId: ItemId) : Int

}