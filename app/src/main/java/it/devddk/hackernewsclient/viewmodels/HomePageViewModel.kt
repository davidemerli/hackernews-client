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

    // Use cases
    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItem: GetItemUseCase by inject()

    /**
     * The [CollectionQueryType] requested by the user (Top Stories, New Stories)
     */
    private val _query: MutableStateFlow<CollectionQueryType> = MutableStateFlow(TopStories)
    val query = _query.asStateFlow()

    /**
     * [NewsItemState] is a [StateFlow] that contains the current state of the [NewsPage].
     * When the state is [NewsPageState.NewsIdsLoaded], it will contain the list of all ids
     */
    val pageState: StateFlow<NewsPageState> = _query.transform { query ->
        // Set loading until it's loaded
        emit(NewsPageState.Loading)
        // Ask for stories and process the result
        getNewStories(query).fold(
            onSuccess = {
                emit(NewsPageState.NewsIdsLoaded(it))
            },
            onFailure = {
                emit(NewsPageState.NewsIdsError(it))
            }
        )
    }.flowOn(Dispatchers.IO) // Do the previous transform in IO dispatcher
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NewsPageState.Loading)

    /**
     * Contains [NewsItemState] in order of visualization
     * Note: THis is a mutable state and should be locked when accesses from multiple therads
     */
    private val itemList: MutableList<NewsItemState> = MutableList(500) { NewsItemState.Loading }
    private val _itemListFlow: MutableSharedFlow<List<NewsItemState>> = MutableSharedFlow(1)
    val itemListFlow = _itemListFlow.asSharedFlow()

    /**
     * Change the [CollectionQueryType] of the [NewsPage]
     * This will empty the loaded articles
     */
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

    /**
     * Request the i-th item in the [CollectionQueryType] list
     * The item will be loaded only if it is not already loaded
     */
    suspend fun requestItem(index: Int) {
        Timber.d("Requesting item $index")
        val currPageState = pageState.value
        // Skip loading if the page item ids are not loaded or if the item is
        // already loaded
        if (currPageState !is NewsPageState.NewsIdsLoaded) {
            return
        }
        synchronized(itemList) {
            if (itemList[index] is NewsItemState.ItemLoaded) {
                return
            }
        }
        // Retrieve the itemId from the position
        val itemId = currPageState.itemsId[index]
        withContext(Dispatchers.IO) {
            // Call getItem use case to get it from firebase
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
        // Notify update of the mutable state
        updateItemList()
    }

    /**
     * Notifies the observer of the flow
     * This method reloads the list
     */
    private suspend fun updateItemList() {
        val result: List<NewsItemState>
        synchronized(itemList) {
            result = itemList.take(query.value.maxAmount)
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
