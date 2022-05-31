package it.devddk.hackernewsclient.domain.repository

import it.devddk.hackernewsclient.domain.model.search.SearchResultsSlice

interface SearchRepository {
    suspend fun searchByRelevance(query : String, page: Int) : Result<SearchResultsSlice>
}