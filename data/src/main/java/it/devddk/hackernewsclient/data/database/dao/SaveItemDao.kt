package it.devddk.hackernewsclient.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.ItemEntity
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import java.lang.Exception
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Dao
abstract class SaveItemDao(val database: LocalDatabase) {

    private val collectionDao: ItemCollectionEntityDao = database.itemCollectionDao()
    private val itemDao: ItemEntityDao = database.itemEntityDao()

    @Transaction
    open suspend fun saveWholeStory(
        collection: UserDefinedItemCollection,
        mainStory: ItemEntity,
        subStories: List<ItemEntity>,
        minTimeFromLastSaveMillis: Int,
        increaseRefCount: Boolean
    ): Int {
        val queryResult = collectionDao.addItemToCollection(ItemCollectionEntity(mainStory.id,
            collection,
            LocalDateTime.now()))

        if (queryResult < 0) {
            throw Exception("ItemCollection insertion failed. Rollback")
        }

        return if(needsToBeSaved(collection, mainStory.id, minTimeFromLastSaveMillis)) {
            val itemsInserted = itemDao.insertItems(subStories)

            val itemInsertedCount = itemsInserted.size
            if (itemInsertedCount <= 0) {
                throw Exception("Item insertion failed. Rollback")
            }
            itemInsertedCount
        } else {
            0
        }
    }

    @Query("SELECT COUNT(*) FROM " +
            " items_collection AS ic " +
            "INNER JOIN items ON items.id = ic.id " +
            "WHERE items.storyId == :itemId AND ic.collection IN ('Favorites','ReadLater')")
    abstract suspend fun computeRefCount(itemId: Int) : Int


    @Transaction
    open suspend fun needsToBeSaved(
        collection: UserDefinedItemCollection,
        mainItemId: Int,
        minTimeFromLastSaveMillis: Int,
    ): Boolean {
        val collectionEntity = collectionDao.getItemCollectionEntity(mainItemId, collection)
        val timeWhenDownloaded = itemDao.getDownloadedTime(mainItemId)
        return timeWhenDownloaded == null ||
                timeWhenDownloaded.until(LocalDateTime.now(),
                    ChronoUnit.SECONDS) > minTimeFromLastSaveMillis ||
                (collectionEntity != null &&
                        collectionEntity.timeAdded.until(LocalDateTime.now(),
                            ChronoUnit.SECONDS) > minTimeFromLastSaveMillis)

    }
}