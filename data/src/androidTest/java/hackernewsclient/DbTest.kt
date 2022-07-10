package hackernewsclient

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.entities.toItemEntity
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlinx.coroutines.test.runTest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private lateinit var itemDao: ItemEntityDao
    private lateinit var db: LocalDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, LocalDatabase::class.java).build()
        itemDao = db.itemEntityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun itemEntityDAO_CRUDWorks() = runTest {
        //Create
        val item1 = generateRandomItem(id = 1, by="author1").toItemEntity()
        itemDao.insertItem(item1)

        //Read
        val item1FromDb = itemDao.getItem(1)
        assertEquals(item1, item1FromDb)

        // Update
        val item1Edited = generateRandomItem(id = 1, by="author2").toItemEntity()
        itemDao.insertItem(item1Edited)
        val item1EditedFromDb = itemDao.getItem(1)
        assertEquals(item1Edited, item1EditedFromDb)

        //Delete
        val nRowsDeleted = itemDao.deleteItem(1)
        assertEquals(1, nRowsDeleted)
        val newItem = itemDao.getItem(1)
        assertNull(newItem)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun itemEntityDAO_multipleInserts() = runTest {
        val item1 = generateRandomItem(id = 1, by="author_different").toItemEntity()
        itemDao.insertItem(item1)
        val items = (0 until 50).map { i -> generateRandomItem(id = i, by = "author_$i").toItemEntity() }
        itemDao.insertItems(items)
        (0 until 50).forEach {
           assertEquals("item $it should match from db", items[it], itemDao.getItem(it))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun itemEntityDAO_helperQueries() = runTest {
        val yesterday = LocalDateTime.now().plusDays(-1).truncatedTo(ChronoUnit.SECONDS)
        val html = "<p>lol</p>"
        val item1 = generateRandomItem(
            id = 1,
            storyId = 0,
            downloaded = yesterday,
            htmlPage = html,
        ).toItemEntity()
        val savedWhen = item1.saved
        itemDao.insertItem(item1)
        assertEquals(yesterday, itemDao.getDownloadedTime(1))
        assertEquals(html, itemDao.getHtml(1))
        assertEquals(0, itemDao.getRootStoryId(1))
        assertEquals(savedWhen, itemDao.getSavedTime(1))

        // Item with everything null
        val item2 = Item(
            id = 2,
            type = ItemType.STORY,
            by = "user",
            time = LocalDateTime.now()
        ).toItemEntity()
        val savedWhen2 = item1.saved
        itemDao.insertItem(item2)
        assertNull(itemDao.getDownloadedTime(2))
        assertNull(itemDao.getHtml(2))
        assertNull(itemDao.getRootStoryId(2))
        assertEquals(savedWhen2, itemDao.getSavedTime(2))

        //Not existent item
        assertNull(itemDao.getDownloadedTime(3))
        assertNull(itemDao.getHtml(3))
        assertNull(itemDao.getRootStoryId(3))
        assertNull(itemDao.getSavedTime(3))

    }
}

