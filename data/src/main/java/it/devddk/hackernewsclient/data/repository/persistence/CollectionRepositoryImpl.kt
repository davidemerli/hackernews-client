package it.devddk.hackernewsclient.data.repository.persistence

import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.domain.model.items.ItemCollectionTag
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.repository.CollectionRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CollectionRepositoryImpl : CollectionRepository, KoinComponent {

    private val localDatabase : LocalDatabase by inject()
    private val collectionDao = localDatabase.itemCollectionDao()


    override suspend fun addItemToCollection(id: ItemId, collection: ItemCollectionTag) {
        collectionDao.addItemToCollection(ItemCollectionEntity(id, collection.name))
    }

    override suspend fun removeItemFromCollection(id: ItemId, collection: ItemCollectionTag) {
        collectionDao.removeItemFromCollection(ItemCollectionEntity(id, collection.name))
    }

    override suspend fun getCollectionsOfItem(id: ItemId): Set<ItemCollectionTag> {
        return collectionDao.getAllCollectionsForItem(id).map {
            ItemCollectionTag.valueOf(it.collection)
        }.toSet()
    }

    override suspend fun getAllItemsForCollection(collection: ItemCollectionTag): List<ItemId> {
        return collectionDao.getAllIdsForCollection(collection.name).map {
            it.id
        }
    }
}