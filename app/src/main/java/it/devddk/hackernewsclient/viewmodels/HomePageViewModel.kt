package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.utils.TopStories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class HomePageViewModel : ViewModel(), KoinComponent {

    companion object {
        const val DEFAULT_REQUEST_UNTIL = 20
    }

    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItem: GetItemUseCase by inject()

    private val _query: MutableStateFlow<CollectionQueryType> = MutableStateFlow(TopStories)
    val query = _query.asStateFlow()

    val pageState: StateFlow<NewsPageState> = _query.transform { query ->
        emit(NewsPageState.Loading)
        getNewStories(query).fold(
            onSuccess = {
                emit(NewsPageState.NewsIdsLoaded(it))
            },
            onFailure = {
                emit(NewsPageState.NewsIdsError(it))
            }
        )
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NewsPageState.Loading)

    private val itemList: MutableList<NewsItemState> = MutableList(500) { NewsItemState.Loading }
    private val _itemListFlow: MutableSharedFlow<List<NewsItemState>> = MutableSharedFlow(1)
    val itemListFlow = _itemListFlow.asSharedFlow()

    suspend fun setQuery(newQuery: CollectionQueryType) {
        val oldQuery = _query.value
        if (oldQuery != newQuery) {
            synchronized(itemList) {
                itemList.clear()
                for (i in 0 until newQuery.maxAmount) {
                    itemList.add(NewsItemState.Loading)
                }
            }
            _query.emit(newQuery)
            updateItemList()
        }
    }

    suspend fun requestItem(index: Int) {
        Timber.d("Requesting item $index")
        val currPageState = pageState.value
        if (currPageState !is NewsPageState.NewsIdsLoaded) {
            return
        }
        synchronized(itemList) {
            if (itemList[index] is NewsItemState.ItemLoaded) {
                return
            }
        }
        val itemId = currPageState.itemsId[index]
        withContext(Dispatchers.IO) {
            getItem(itemId).fold(
                onSuccess = {
                    synchronized(itemList) {
                        itemList[index] = NewsItemState.ItemLoaded(it)
                    }
                },
                onFailure = {
                    synchronized(itemList) {
                        itemList[index] = NewsItemState.ItemError(it)
                    }
                })
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
