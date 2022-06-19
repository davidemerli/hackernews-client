package it.devddk.hackernewsclient.data.repository.persistence

import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.domain.model.utils.SavedItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.repository.CollectionRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CollectionRepositoryImpl : CollectionRepository, KoinComponent {

    private val localDatabase : LocalDatabase by inject()
    private val collectionDao = localDatabase.itemCollectionDao()


    override suspend fun addItemToCollection(id: ItemId, collection: SavedItemCollection) {
        collectionDao.addItemToCollection(ItemCollectionEntity(id, collection::class.simpleName!!))
    }

    override suspend fun removeItemFromCollection(id: ItemId, collection: SavedItemCollection) {
        collectionDao.removeItemFromCollection(ItemCollectionEntity(id, collection::class.simpleName!!))
    }

    override suspend fun getCollectionsOfItem(id: ItemId): Set<SavedItemCollection> {
        return collectionDao.getAllCollectionsForItem(id).map {
            SavedItemCollection.valueOf(it.collection)!!
        }.toSet()
    }

    override suspend fun getAllItemsForCollection(collection: SavedItemCollection): List<ItemId> {
        return collectionDao.getAllIdsForCollection(collection::class.simpleName!!).map {
            it.id
        }
    }
}