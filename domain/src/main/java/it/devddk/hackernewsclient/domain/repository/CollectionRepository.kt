package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.items.ItemCollectionTag
import it.devddk.hackernewsclient.domain.model.utils.ItemId

interface CollectionRepository {
    fun addItemToCollection(id: ItemId, collection: ItemCollectionTag)

    fun removeItemFromCollection(id: ItemId, collection: ItemCollectionTag)

    fun getCollectionsOfItem(id: ItemId) : Set<ItemCollectionTag>

    fun getAllItemsForCollection(collection: ItemCollectionTag) : List<ItemId>

}