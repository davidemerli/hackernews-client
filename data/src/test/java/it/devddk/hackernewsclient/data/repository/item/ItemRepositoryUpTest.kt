package it.devddk.hackernewsclient.data.repository.item

import it.devddk.hackernewsclient.data.di.networkingModule
import it.devddk.hackernewsclient.data.di.repositoryModule
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.inject
import org.koin.test.KoinTestRule
import org.koin.test.KoinTest


class ItemRepositoryUpTest : KoinTest {

    val itemRepository : ItemRepository by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(repositoryModule, networkingModule)
    }

    @Test
    fun getItemNumberSuccedes() : Unit {
        runBlocking {
            val result = itemRepository.getItemById(1)
            result.getOrThrow()
            assertTrue(result.isSuccess)
        }
    }



}