package it.devddk.hackernewsclient.domain.model.search

import it.devddk.hackernewsclient.domain.model.items.Item

data class SearchResult(val item: Item, val searchMetadata: SearchResultMetaData)

data class SearchResultsSlice(val index : Int, val startPosition: Int, val results: List<SearchResult>) {
    val endPosition : Int
        get() = startPosition + results.size
}