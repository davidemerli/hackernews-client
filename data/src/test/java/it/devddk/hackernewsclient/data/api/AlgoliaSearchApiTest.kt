package it.devddk.hackernewsclient.data.api

import it.devddk.hackernewsclient.data.di.networkingModule
import it.devddk.hackernewsclient.data.di.repositoryModule
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject

class AlgoliaSearchApiTest : KoinTest {

    private val api : AlgoliaSearchApi by inject()

    @Before
    fun setup() {
        startKoin {
            modules(networkingModule)
        }
    }

    @Test
    fun algoliaSearchApiTest() {
        runBlocking {
            assertTrue(api.searchByRelevance("pippo", 0).isSuccessful)
        }
    }

    @After
    fun teardown() {
        stopKoin()
    }


}