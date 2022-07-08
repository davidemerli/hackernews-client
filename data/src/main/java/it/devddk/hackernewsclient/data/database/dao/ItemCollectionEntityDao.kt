package it.devddk.hackernewsclient.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntityPrimaryKey

@Dao
interface ItemCollectionEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItemToCollection(entry: ItemCollectionEntity) : Long

    @Query("DELETE FROM items_collection WHERE :id = id AND :collection = collection")
    suspend fun removeItemFromCollection(id: Int, collection: String) : Int

    @Query("SELECT * FROM items_collection AS ic WHERE :id = ic.id")
    suspend fun getAllCollectionsForItem(id: Int) : List<ItemCollectionEntity>

    @Query("SELECT * FROM items_collection AS ic WHERE :collection = ic.collection")
    suspend fun getAllIdsForCollection(collection: String) : List<ItemCollectionEntity>

}