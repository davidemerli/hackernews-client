package it.devddk.hackernewsclient.data.database.dao

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Transaction
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.entities.ItemCollectionEntity
import it.devddk.hackernewsclient.data.database.entities.ItemEntity
import java.lang.Exception
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Dao
abstract class SaveItemDao(val database: LocalDatabase) {

    private val collectionDao: ItemCollectionEntityDao = database.itemCollectionDao()
    private val itemDao: ItemEntityDao = database.itemEntityDao()

    @Transaction
    suspend fun saveWholeStory(
        collection: String,
        mainStory: ItemEntity,
        subStories: List<ItemEntity>,
        minTimeFromLastSaveMillis: Int,
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private suspend fun needsToBeSaved(
        collection: String,
        mainItemId: Int,
        minTimeFromLastSaveMillis: Int,
    ): Boolean {
        val collectionEntity = collectionDao.getItemCollectionEntity(mainItemId, collection)
        val timeWhenSaved = itemDao.getSavedTime(mainItemId)
        return timeWhenSaved == null ||
                timeWhenSaved.until(LocalDateTime.now(),
                    ChronoUnit.SECONDS) > minTimeFromLastSaveMillis ||
                (collectionEntity != null &&
                        collectionEntity.timeAdded.until(LocalDateTime.now(),
                            ChronoUnit.SECONDS) > minTimeFromLastSaveMillis)

    }

    @Transaction
    suspend fun removeWholeStoryIfUnused(
        collToRemove: ItemCollectionEntity,
        subStories: List<ItemEntity>,
    ) {

    }
}