package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.HNItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.utils.TopStories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class HomePageViewModel : ViewModel(), KoinComponent {

    // internal state
    private val itemList: MutableList<NewsItemState> = MutableList(500) { NewsItemState.Loading }

    // use cases
    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItemById: GetItemUseCase by inject()

    // state flows
    private val _uiState: MutableStateFlow<HNItemCollection> = MutableStateFlow(TopStories)
    val uiState = _uiState.asStateFlow()

    private val _itemListFlow: MutableSharedFlow<List<NewsItemState>> = MutableSharedFlow(1)
    val itemListFlow = _itemListFlow.asSharedFlow()

    val pageState: StateFlow<NewsPageState> = _uiState.transform { query ->
        emit(NewsPageState.Loading)

        getNewStories(query).fold(
            onSuccess = {
                synchronized(itemList) {
                    itemList.clear()

                    it.indices.forEach { _ ->
                        itemList.add(NewsItemState.Loading)
                    }
                }

                emit(NewsPageState.NewsIdsLoaded(it))
            },
            onFailure = {
                emit(NewsPageState.NewsIdsError(it))
            }
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NewsPageState.Loading
    )

    suspend fun setQuery(newQuery: HNItemCollection) {
        val oldQuery = _uiState.value

        if (oldQuery != newQuery) {
            _uiState.emit(newQuery)

            updateItemList()
        }
    }

    suspend fun requestItem(index: Int) {
        Timber.d("Requesting item $index")

        val currPageState = pageState.value

        // avoid requesting item if the page is not loaded
        if (currPageState !is NewsPageState.NewsIdsLoaded) return

        synchronized(itemList) {
            // avoid requesting item if it's already loaded
            if (itemList[index] is NewsItemState.ItemLoaded) return
        }

        val itemId = currPageState.itemsId[index]

        withContext(Dispatchers.IO) {
            getItemById(itemId).fold(
                onSuccess = {
                    synchronized(itemList) {
                        itemList[index] = NewsItemState.ItemLoaded(it)
                    }
                },
                onFailure = {
                    synchronized(itemList) {
                        itemList[index] = NewsItemState.ItemError(it)
                    }
                }
            )
        }

        updateItemList()
    }

    private suspend fun updateItemList() {
        val result: List<NewsItemState>

        synchronized(itemList) {
            result = itemList.toList()
        }

        _itemListFlow.emit(result)
    }
}

sealed class NewsPageState {
    object Loading : NewsPageState()
    data class NewsIdsLoaded(val itemsId: List<ItemId>) : NewsPageState()
    data class NewsIdsError(val exception: Throwable) : NewsPageState()
}

sealed class NewsItemState {
    object Loading : NewsItemState()
    data class ItemLoaded(val item: Item) : NewsItemState()
    data class ItemError(val exception: Throwable) : NewsItemState()
}
