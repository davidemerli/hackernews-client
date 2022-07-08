package it.devddk.hackernewsclient.data.repository.search

import it.devddk.hackernewsclient.data.api.AlgoliaSearchApi
import it.devddk.hackernewsclient.data.networking.base.asResult
import it.devddk.hackernewsclient.domain.model.search.SearchQuery
import it.devddk.hackernewsclient.domain.model.search.SearchResultsSlice
import it.devddk.hackernewsclient.domain.repository.SearchRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchRepositoryImpl : SearchRepository, KoinComponent {

    private val searchApi: AlgoliaSearchApi by inject()

    override suspend fun searchByRelevance(
        query: SearchQuery,
        page: Int,
    ): Result<SearchResultsSlice> {

        return searchApi.searchByRelevance(
            page = page,
            query = query.query,
            tags = query.tags,
            numericFilters = query.numericalFilters
        ).asResult().mapCatching {
            it.mapToDomainModel()
        }
    }

    override suspend fun searchByTime(query: SearchQuery, page: Int): Result<SearchResultsSlice> {
        return searchApi.searchByTime(
            page = page,
            query = query.query,
            tags = query.tags,
            numericFilters = query.numericalFilters
        ).asResult().mapCatching {
            val resultSlice = it.mapToDomainModel()

            resultSlice
        }
    }
}