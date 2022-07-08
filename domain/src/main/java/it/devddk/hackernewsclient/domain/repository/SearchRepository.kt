package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.search.SearchQuery
import it.devddk.hackernewsclient.domain.model.search.SearchResultsSlice

interface SearchRepository {
    suspend fun searchByRelevance(query : SearchQuery, page: Int) : Result<SearchResultsSlice>

    suspend fun searchByTime(query : SearchQuery, page: Int) : Result<SearchResultsSlice>
}