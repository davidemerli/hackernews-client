package it.devddk.hackernewsclient.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.devddk.hackernewsclient.data.database.entities.ItemEntity
import it.devddk.hackernewsclient.data.database.entities.ItemEntityWithoutHtmlPage
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

    @Query("SELECT id, deleted, author, type," +
            " time, dead, parent, text, kids, " +
            "title, descendants, parts, poll, " +
            "score, url " +
            "FROM items WHERE :itemId = items.id LIMIT 1")
    suspend fun getItemWithoutHtml(itemId: ItemId): ItemEntityWithoutHtmlPage

    @Query("SELECT saved " +
            "FROM items WHERE :itemId = items.id LIMIT 1")
    suspend fun getSavedTime(itemId: ItemId): LocalDateTime?


    @Query("SELECT htmlPage FROM items WHERE :itemId = items.id LIMIT 1")
    suspend fun getHtml(itemId: ItemId): String?

}