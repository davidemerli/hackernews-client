package it.devddk.hackernewsclient.data.repository.item

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkClass
import it.devddk.hackernewsclient.data.DispatcherProviderTesting
import it.devddk.hackernewsclient.data.common.utils.Connectivity
import it.devddk.hackernewsclient.data.database.dao.ItemCollectionEntityDao
import it.devddk.hackernewsclient.data.database.dao.ItemEntityDao
import it.devddk.hackernewsclient.data.database.entities.ItemEntity
import it.devddk.hackernewsclient.data.di.databaseModule
import it.devddk.hackernewsclient.data.di.networkingModule
import it.devddk.hackernewsclient.data.di.repositoryModule
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declare
import org.koin.test.mock.declareMock
import org.mockito.junit.MockitoJUnitRunner
import timber.log.Timber
import java.lang.IllegalStateException
import java.time.LocalDateTime
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class ItemRepositoryTestDatabase : KoinTest {
    val dut = ItemRepositoryImpl()


    private val entity = ItemEntity(
        1,
        false,
        "giovanni",
        1,
        "STORY",
        LocalDateTime.now(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        false,
        null,
        null,
        emptyList(),
        "Sono giovanni",
        "Sono giovanni",
        0,
        emptyList(),
        null,
        10,
        "www.com",
        null
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val instantiateModules = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(repositoryModule, networkingModule, databaseModule)
        Timber.plant(Timber.DebugTree())
    }

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        // Your way to build a Mock here
        mockkClass(clazz)

    }



    @Before
    fun setup() {
        runBlocking {
            dut.invalidateCache()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getItemById_databaseOnly() = runTest {

        declare {
           DispatcherProviderTesting(
                UnconfinedTestDispatcher(testScheduler),
                UnconfinedTestDispatcher(testScheduler),
                UnconfinedTestDispatcher(testScheduler),
                UnconfinedTestDispatcher(testScheduler)
           )
        }

        declareMock<ItemEntityDao> {
            coEvery { getItem(1) } returns entity
            coEvery { getItem(2) } returns null
            coEvery { getRootStoryId(any()) } returns 1
        }

        declareMock<Connectivity> {
            every { hasNetworkAccess() } returns false
        }

        declareMock<ItemCollectionEntityDao>() {
            coEvery { getAllCollectionsForItem(any()) } returns emptyList()

        }

        val result1 = dut.getItemById(1, ItemRepository.FetchMode(ItemRepository.Online, ItemRepository.Offline))
        assert(result1.isSuccess)
        assertEquals(result1.getOrNull()!!.id, entity.id)
        assertEquals(result1.getOrNull()!!.url, entity.url)
        val result2 = dut.getItemById(2, ItemRepository.FetchMode(ItemRepository.Offline))
        assert(result2.isFailure)
        assertThrows(IllegalStateException::class.java) { result2.getOrThrow() }
    }
}