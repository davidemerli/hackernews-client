package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection


interface ItemRepository {
    /**
     * Gets an item by is [ItemId]
     */
    suspend fun getItemById(itemId: ItemId, forceRefresh : Boolean = false): Result<Item>

    suspend fun saveItem(item : Item)

    suspend fun addItemToCollection(id: ItemId, collection: UserDefinedItemCollection) : Result<Unit>

    suspend fun removeItemFromCollection(id: ItemId, collection: UserDefinedItemCollection) : Result<Unit>

    suspend fun getCollectionsOfItem(id: ItemId) : Result<Set<UserDefinedItemCollection>>

    suspend fun getAllItemsForCollection(collection: UserDefinedItemCollection) : Result<List<ItemId>>

    suspend fun invalidateCache() {
        // Do nothing by default, if cache available in implementation, handle invalidate
    }
}