package it.devddk.hackernewsclient.domain.model.search

import it.devddk.hackernewsclient.domain.model.items.Item

data class SearchedItem(val item: Item, val searchMetadata: SearchResultMetaData)

data class SearchResultsSlice(val startPosition: Int, val results: List<SearchedItem>) {
    val endPosition : Int
        get() = startPosition + results.size
}