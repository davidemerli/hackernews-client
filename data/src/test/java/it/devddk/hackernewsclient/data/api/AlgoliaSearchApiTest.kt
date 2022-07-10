package it.devddk.hackernewsclient.data.api

import it.devddk.hackernewsclient.data.di.networkingModule
import it.devddk.hackernewsclient.data.networking.base.asResult
import it.devddk.hackernewsclient.domain.model.search.NumericalSearchFilters
import it.devddk.hackernewsclient.domain.model.search.SearchFilter
import it.devddk.hackernewsclient.domain.model.search.SearchTags
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject

class AlgoliaSearchApiTest : KoinTest {

    private val api: AlgoliaSearchApi by inject()

    @Before
    fun setup() {
        startKoin {
            modules(networkingModule)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun algoliaSearchApiTest_isUp() = runTest {
        assertTrue("Algolia should be up", api.getItemById(10).asResult().isSuccess)
        assertTrue("Algolia should be up", api.searchByRelevance(page = 1).asResult().isSuccess)
        assertTrue("Algolia should be up", api.searchByTime(page = 1).asResult().isSuccess)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun algoliaSearchApiTest_covertsTypes() = runTest {
        assertTrue("Algolia should be up",
            api.searchByRelevance("sas",
                SearchTags.AllTags,
                NumericalSearchFilters(listOf(SearchFilter.POINTS greaterThan 10)),
                1).asResult().isSuccess)
    }

    @After
    fun teardown() {
        stopKoin()
    }
}