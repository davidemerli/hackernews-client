package hackernewsclient.database.dao

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import hackernewsclient.generateRandomItem
import hackernewsclient.generateRandomItemCollectionEntity
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.dao.SaveItemDao
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlinx.coroutines.test.runTest

@RunWith(AndroidJUnit4::class)
class SaveItemDaoIntegrationTest {
    private lateinit var saveItemDao: SaveItemDao
    private lateinit var itemDao: ItemEntityDao
    private lateinit var collectionDao: ItemCollectionEntityDao
    private lateinit var db: LocalDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, LocalDatabase::class.java).build()
        saveItemDao = db.saveItemDao()
        itemDao = db.itemEntityDao()
        collectionDao = db.itemCollectionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun saveItemDao_isCorrect() = runTest {

        val timeoutMs = 2000
        val items = (1..30).map {
            generateRandomItem(id = it, by = "user$it", storyId =1).toItemEntity()
        }
        val ic1 = generateRandomItemCollectionEntity(1, UserDefinedItemCollection.ReadLater)
        val ic2 = generateRandomItemCollectionEntity(4, UserDefinedItemCollection.Favorites)
        db.withTransaction {
            assertEquals(0, saveItemDao.computeRefCount(1))
            collectionDao.addItemToCollection(ic1)
        }
        assertTrue(saveItemDao.needsToBeSaved(UserDefinedItemCollection.ReadLater, 1, timeoutMs))

        itemDao.insertItems(items)
        assertEquals(1, saveItemDao.computeRefCount(1))
        collectionDao.addItemToCollection(ic2)
        assertFalse(saveItemDao.needsToBeSaved(UserDefinedItemCollection.ReadLater, 1, timeoutMs))
        assertEquals(2, saveItemDao.computeRefCount(1))
    }

}