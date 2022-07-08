package it.devddk.hackernewsclient.domain.interaction.search

import it.devddk.hackernewsclient.domain.model.search.SearchQuery
import it.devddk.hackernewsclient.domain.model.search.SearchResultsSlice
import it.devddk.hackernewsclient.domain.repository.SearchRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AdvancedSearchItemByRelevanceUseCase {
    suspend operator fun invoke(
        query: SearchQuery,
        page: Int,
    ): Result<SearchResultsSlice>
}

class AdvancedSearchItemByRelevanceUseCaseImpl : AdvancedSearchItemByRelevanceUseCase,
    KoinComponent {

    private val searchRepository: SearchRepository by inject()

    override suspend operator fun invoke(
        query: SearchQuery,
        page: Int,
    ): Result<SearchResultsSlice> {
        return searchRepository.searchByRelevance(query, page)
    }


}