package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import it.devddk.hackernewsclient.domain.interaction.search.AdvancedSearchItemByRelevanceUseCase
import it.devddk.hackernewsclient.domain.model.search.SearchQuery
import it.devddk.hackernewsclient.domain.model.search.SearchResult
import it.devddk.hackernewsclient.domain.model.search.SearchResultsSlice
import it.devddk.hackernewsclient.domain.utils.takeWhileInclusive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.ConcurrentSkipListMap

class SearchPageViewModel : ViewModel(), KoinComponent {

    private val advancedSearchByRelevance: AdvancedSearchItemByRelevanceUseCase by inject()

    // FIXME Change this with advanced search
    private val _searchQuery = MutableStateFlow(SearchQuery())
    private val searchQuery = _searchQuery.asStateFlow()

    private val pages: ConcurrentSkipListMap<Int, SliceLoadingState> = ConcurrentSkipListMap()
    private val _resultListFlow = MutableSharedFlow<List<SearchResultUiState>>(1)
    val resultListFlow = _resultListFlow.asSharedFlow()

    suspend fun updateSimpleQuery(query: String) {
        val oldQuery = searchQuery.value
        val newQuery = SearchQuery(query)

        if (oldQuery != newQuery) {
            pages.clear()
            _searchQuery.emit(newQuery)
            updateResultList()
            requestItem(0)
        }
    }

    suspend fun updateAdvancedQuery(query: SearchQuery) {
        val oldQuery = searchQuery.value

        if (oldQuery != query) {
            pages.clear()
            _searchQuery.emit(query)
            updateResultList()
            requestItem(0)
        }
    }

    suspend fun requestItem(position: Int) {
        val pageIndex = position.div(20)

        val newPageState = synchronized(pages) {
            when (val page = pages[pageIndex]) {
                null, is SliceLoadingState.Error -> {
                    SliceLoadingState.Loading(pageIndex)
                }
                else -> {
                    page
                }
            }
        }

        if (newPageState !is SliceLoadingState.Loading) {
            return
        }
        // Request item
        Timber.d("Requesting")
        advancedSearchByRelevance(searchQuery.value, pageIndex).fold(
            onSuccess = {
                pages[pageIndex] = SliceLoadingState.SliceLoaded(it)
                updateResultList()
            },
            onFailure = {
                pages[pageIndex] = SliceLoadingState.Error(it)
                Timber.e("Error onFailure ${it.stackTraceToString()}")
                updateResultList()
            }
        )
    }

    private suspend fun updateResultList() {
        val newList = pages.map { (_, page) ->
            page
        }.takeWhileInclusive { it is SliceLoadingState.SliceLoaded }
            .flatMap { page -> page.unrollToUiStates() }
        Timber.d("$pages")
        _resultListFlow.emit(newList)
    }
}

private sealed class SliceLoadingState {

    abstract fun unrollToUiStates(): List<SearchResultUiState>

    data class Loading(val position: Int) : SliceLoadingState() {
        override fun unrollToUiStates() =
            listOf<SearchResultUiState>(SearchResultUiState.Loading(position))
    }

    data class SliceLoaded(val slice: SearchResultsSlice) : SliceLoadingState() {
        override fun unrollToUiStates() = slice.results.map {
            SearchResultUiState.ResultLoaded(it)
        }
    }

    data class Error(val throwable: Throwable) : SliceLoadingState() {
        override fun unrollToUiStates() = emptyList<SearchResultUiState>()
    }
}

sealed class SearchResultUiState {
    data class Loading(val position: Int) : SearchResultUiState()
    data class ResultLoaded(val result: SearchResult) : SearchResultUiState()
}
