package hackernewsclient.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import hackernewsclient.generateRandomItemCollectionEntity
import it.devddk.hackernewsclient.data.database.LocalDatabase
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlinx.coroutines.test.runTest

@RunWith(AndroidJUnit4::class)
class ItemCollectionEntityUnitTest {
    private lateinit var collectionDao: ItemCollectionEntityDao
    private lateinit var db: LocalDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, LocalDatabase::class.java).build()
        collectionDao = db.itemCollectionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun itemCollectionDao_CRUD() = runTest {
        val ic1 = generateRandomItemCollectionEntity(id = 1, UserDefinedItemCollection.ReadLater)
        // Create
        collectionDao.addItemToCollection(ic1)
        // Read
        val ic1FromDb =
            collectionDao.getItemCollectionEntity(1, UserDefinedItemCollection.ReadLater)
        assertEquals(ic1, ic1FromDb)
        // Update (the time is different)
        val ic2 = generateRandomItemCollectionEntity(id = 1, UserDefinedItemCollection.ReadLater)
        val ic2FromDb =
            collectionDao.getItemCollectionEntity(1, UserDefinedItemCollection.ReadLater)
        collectionDao.addItemToCollection(ic2)
        assertEquals(ic2, ic2FromDb)
        // Insert (insert different collections)
        val ic3 = generateRandomItemCollectionEntity(id = 1, UserDefinedItemCollection.Favorites)
        collectionDao.addItemToCollection(ic3)
        val ic3FromDb =
            collectionDao.getItemCollectionEntity(1, UserDefinedItemCollection.Favorites)
        collectionDao.addItemToCollection(ic3)
        assertEquals(ic3, ic3FromDb)
        // Delete
        val rowsDeleted1 =
            collectionDao.removeItemFromCollection(1, UserDefinedItemCollection.ReadLater)
        assertEquals(1, rowsDeleted1)
        val result = collectionDao.getItemCollectionEntity(1, UserDefinedItemCollection.ReadLater)
        assertNull(result)
        val ic3FromDb2 =
            collectionDao.getItemCollectionEntity(1, UserDefinedItemCollection.Favorites)
        collectionDao.addItemToCollection(ic3)
        assertEquals(ic3, ic3FromDb2)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun itemCollectionDao_supportMethods() = runTest {
        val items = (1..50).flatMap {
            listOf(
                generateRandomItemCollectionEntity(id = it, UserDefinedItemCollection.ReadLater),
                generateRandomItemCollectionEntity(id = it + 1, UserDefinedItemCollection.Favorites)
            )
        }
        items.forEach {
            collectionDao.addItemToCollection(it)
        }
        assertEquals(setOf(UserDefinedItemCollection.ReadLater),
            collectionDao.getAllCollectionsForItem(1).map { it.collection }.toSet())
        assertEquals(setOf(UserDefinedItemCollection.Favorites),
            collectionDao.getAllCollectionsForItem(51).map { it.collection }.toSet())
        assertEquals(setOf(UserDefinedItemCollection.ReadLater,
            UserDefinedItemCollection.Favorites),
            collectionDao.getAllCollectionsForItem(2).map { it.collection }.toSet())
        assertEquals(emptySet<UserDefinedItemCollection>(),
            collectionDao.getAllCollectionsForItem(52).map { it.collection }.toSet())
        assertEquals((1..50).toSet(),
            collectionDao.getAllIdsForCollection(UserDefinedItemCollection.ReadLater).map { it.id }
                .toSet())
        assertEquals((2..51).toSet(),
            collectionDao.getAllIdsForCollection(UserDefinedItemCollection.Favorites).map { it.id }
                .toSet())
        assertEquals(emptySet<ItemId>(),
            collectionDao.getAllIdsForCollection(UserDefinedItemCollection.VisitedItem)
                .map { it.id }.toSet())
    }
}