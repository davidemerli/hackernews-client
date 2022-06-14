package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.items.ItemCollectionTag
import it.devddk.hackernewsclient.domain.model.utils.ItemId

interface CollectionRepository {
    suspend fun addItemToCollection(id: ItemId, collection: ItemCollectionTag)

    suspend fun removeItemFromCollection(id: ItemId, collection: ItemCollectionTag)

    suspend fun getCollectionsOfItem(id: ItemId) : Set<ItemCollectionTag>

    suspend fun getAllItemsForCollection(collection: ItemCollectionTag) : List<ItemId>

}