package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.utils.SavedItemCollection

interface CollectionRepository {
    suspend fun addItemToCollection(id: ItemId, collection: SavedItemCollection)

    suspend fun removeItemFromCollection(id: ItemId, collection: SavedItemCollection)

    suspend fun getCollectionsOfItem(id: ItemId) : Set<SavedItemCollection>

    suspend fun getAllItemsForCollection(collection: SavedItemCollection) : List<ItemId>

}