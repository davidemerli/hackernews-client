package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.devddk.hackernewsclient.domain.interaction.collection.AddItemToCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.collection.RemoveItemFromCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.interaction.item.RefreshAllItemsUseCase
import it.devddk.hackernewsclient.domain.interaction.item.RefreshAllItemsUseCaseImpl
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.utils.getValue
import it.devddk.hackernewsclient.domain.utils.requireValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

class HomePageViewModel : ViewModel(), KoinComponent {

    // internal state
    private val items: MutableMap<ItemId, NewsItemState> = ConcurrentHashMap()

    // use cases
    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItemById: GetItemUseCase by inject()
    private val addToCollection: AddItemToCollectionUseCase by inject()
    private val removeFromCollection: RemoveItemFromCollectionUseCase by inject()
    private val refreshAllItems: RefreshAllItemsUseCase by inject()

    // state flows
    private val _currentQuery: MutableSharedFlow<ItemCollection> = MutableSharedFlow<ItemCollection>(1).apply {
        onSubscription { emit(TopStories) }
    }
    val currentQuery = _currentQuery.asSharedFlow()

    private val _itemListFlow: MutableSharedFlow<List<NewsItemState>> = MutableSharedFlow(1)
    val itemListFlow = _itemListFlow.asSharedFlow()

    val pageState: StateFlow<NewsPageState> = _currentQuery.transform { query ->
        emit(NewsPageState.Loading)

        getNewStories(query).fold(
            onSuccess = {
                synchronized(items) {
                    items.clear()

                    it.indices.forEach { id ->
                        items[id] = NewsItemState.Loading
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

    suspend fun setQuery(newQuery: ItemCollection) {
        val oldQuery = _currentQuery.getValue()

        if (oldQuery != newQuery) {
            _currentQuery.emit(newQuery)

            updateItemList()
        }
    }

    suspend fun requestItem(index: Int) {
        Timber.d("Requesting item $index")

        val currPageState = pageState.value

        // avoid requesting item if the page is not loaded
        if (currPageState !is NewsPageState.NewsIdsLoaded) return

        val itemId = currPageState.itemsId[index]

        synchronized(items) {
            // avoid requesting item if it's already loaded
            if (items[itemId] is NewsItemState.ItemLoaded) return
        }

        withContext(Dispatchers.IO) {
            getItemById(itemId).fold(
                onSuccess = {
                    synchronized(items) {
                        items[itemId] = NewsItemState.ItemLoaded(it)
                    }
                },
                onFailure = {
                    Timber.e("Failed to retrieve item $index. ${it.printStackTrace()}")
                    synchronized(items) {
                        items[itemId] = NewsItemState.ItemError(it)
                    }
                }
            )
        }

        updateItemList()
    }

    suspend fun refreshItem(itemId: Int) {
        getItemById(itemId).onSuccess {
            synchronized(items) {
                items[itemId] = NewsItemState.ItemLoaded(it)
            }
            updateItemList()
        }
    }

    suspend fun refreshPage() {
        refreshAllItems()
        _currentQuery.emit(_currentQuery.requireValue())
        updateItemList()
    }

    suspend fun addToFavorite(itemId: Int, collection: UserDefinedItemCollection) {
        getItemIfLoaded(itemId)
            ?: throw IllegalStateException("Cannot add to favourite a non loaded item")
        val result = addToCollection(itemId, collection).onFailure {
            Timber.e(it, "Failed to add to collection")
        }
        if (result.isSuccess) {
            refreshItem(itemId)
        }
    }


    suspend fun removeFromFavorite(itemId: ItemId, collection: UserDefinedItemCollection) {
        getItemIfLoaded(itemId)
            ?: throw IllegalStateException("Cannot remove from favourite a non loaded item")
        val result = removeFromCollection(itemId, collection).onFailure {
            Timber.e(it, "Failed to remove from collection")
        }
        if (result.isSuccess) {
            refreshItem(itemId)
        }
    }

    private fun getItemIfLoaded(itemId: Int): Item? {
        synchronized(items) {
            return (items[itemId] as? NewsItemState.ItemLoaded)?.item
        }
    }

    private suspend fun updateItemList() {
        val outputList: List<NewsItemState>?
        synchronized(items) {
            val itemState = pageState.value as? NewsPageState.NewsIdsLoaded
            outputList = itemState?.itemsId?.map { id ->
                items[id] ?: NewsItemState.Loading
            }
        }

        if (outputList != null) {
            Timber.d("Updated List")
            _itemListFlow.emit(outputList)
        }

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
