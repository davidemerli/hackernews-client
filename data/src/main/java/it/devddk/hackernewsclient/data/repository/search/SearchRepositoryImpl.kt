package it.devddk.hackernewsclient.data.repository.search

import it.devddk.hackernewsclient.data.api.AlgoliaSearchApi
import it.devddk.hackernewsclient.data.networking.base.asResult
import it.devddk.hackernewsclient.domain.model.search.SearchResultsSlice
import it.devddk.hackernewsclient.domain.repository.SearchRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchRepositoryImpl : SearchRepository, KoinComponent {

    private val searchApi : AlgoliaSearchApi by inject()

    override suspend fun searchByRelevance(query: String, page: Int) : Result<SearchResultsSlice> {
        return searchApi.searchByRelevance(query, page).asResult().mapCatching {
            it.mapToDomainModel()
        }
    }
}