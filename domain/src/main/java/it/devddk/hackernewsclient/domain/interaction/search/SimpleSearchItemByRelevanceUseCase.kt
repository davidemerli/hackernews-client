package it.devddk.hackernewsclient.domain.interaction.search

import it.devddk.hackernewsclient.domain.model.search.SearchQuery
import it.devddk.hackernewsclient.domain.model.search.SearchResultsSlice
import it.devddk.hackernewsclient.domain.model.search.SearchTags
import it.devddk.hackernewsclient.domain.repository.SearchRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SimpleSearchItemByRelevanceUseCase {
    suspend operator fun invoke(
        query: String,
        page: Int,
    ): Result<SearchResultsSlice>
}

class SimpleSearchItemByRelevanceUseCaseImpl : SimpleSearchItemByRelevanceUseCase, KoinComponent {

    private val searchRepository: SearchRepository by inject()

    override suspend operator fun invoke(
        query: String,
        page: Int,
    ): Result<SearchResultsSlice> {
        return searchRepository.searchByRelevance(SearchQuery(query=query, tags=SearchTags.NoPolls), page)
    }
}