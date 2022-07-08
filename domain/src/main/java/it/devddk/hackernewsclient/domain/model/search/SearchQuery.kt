package it.devddk.hackernewsclient.domain.model.search

data class SearchQuery(
    val query: String? = null,
    val tags: SearchTags? = null,
    val numericalFilters: NumericalSearchFilters? = null
)