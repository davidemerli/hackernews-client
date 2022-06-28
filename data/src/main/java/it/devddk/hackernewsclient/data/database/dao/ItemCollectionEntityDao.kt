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

    /**
     * Adds an item to a specific collection, replaces previous one if key is identical
     * @param entry The [ItemCollectionEntity] represeting the entry
     * @return A number >= 0 if successful, < 0 other wise
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItemToCollection(entry: ItemCollectionEntity) : Long

    /**
     * Removes an item from a specific collection
     * @param id The id of the item that should be removed from a collection
     * @param collection The name of the collection from where the item should be removed. The name
     *                   of the collection should correspond to the class simple name of the
     *                   corresponding [ItemCollection].
     * @return Number of row deleted. If 0 the query failed (or did not find any match to remove)
     */
    @Query("DELETE FROM items_collection WHERE :id = id AND :collection = collection")
    suspend fun removeItemFromCollection(id: Int, collection: String) : Int

    /**
     * Fetches the [ItemCollectionEntity] from its primary key if it exists
     * @param id The id of item in the [ItemCollectionEntity]
     * @param collection The collection referred in [ItemCollectionEntity]
     * @return The searched [ItemCollectionEntity], or null if it does not exist
     */
    @Query("SELECT * FROM items_collection WHERE :id = id AND :collection = collection LIMIT 1")
    suspend fun getItemCollectionEntity(id: Int, collection: String) : ItemCollectionEntity?

    /**
     * Gets all collections where the item was put into
     * @param id The item id
     * @return A list of all collection where the item is inside
     */
    @Query("SELECT * FROM items_collection AS ic WHERE :id = ic.id")
    suspend fun getAllCollectionsForItem(id: Int) : List<ItemCollectionEntity>

    /**
     * Gets all items inside collection
     * @param collection The name of the ItemCollection (java simple name of the collection object)
     * @return All items id inside the collection (wrapped inside [ItemCollectionEntity])
     */
    @Query("SELECT * FROM items_collection AS ic WHERE :collection = ic.collection")
    suspend fun getAllIdsForCollection(collection: String) : List<ItemCollectionEntity>

}