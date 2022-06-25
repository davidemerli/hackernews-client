package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.devddk.hackernewsclient.domain.interaction.collection.AddItemToCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.collection.RemoveItemFromCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.interaction.item.RefreshAllItemsUseCase
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.utils.getValue
import it.devddk.hackernewsclient.domain.utils.requireValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

@Deprecated("This view model will be soon replaced with a new more powerful one")
class NewsListViewModel : ViewModel(), KoinComponent {

    // internal state
    private val items: MutableMap<ItemId, NewsItemState> = ConcurrentHashMap()

    // use cases
    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItemById: GetItemUseCase by inject()
    private val addToCollection: AddItemToCollectionUseCase by inject()
    private val removeFromCollection: RemoveItemFromCollectionUseCase by inject()
    private val refreshAllItems: RefreshAllItemsUseCase by inject()

    // state flows
    private val _currentQuery: MutableSharedFlow<ItemCollection> =
        MutableSharedFlow<ItemCollection>(1).apply {
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

                    it.forEachIndexed { index, id ->
                        items[index] = NewsItemState.Loading(id)
                    }
                }

                emit(NewsPageState.NewsIdsLoaded(it))
                updateItemList()
            },
            onFailure = {
                emit(NewsPageState.NewsIdsError(it))
                updateItemList()
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

        // FIXME: items not in the list at this stage are getting requested in favorites screen
        if (index >= currPageState.itemsId.size) {
            Timber.d("Requesting item $index, but it is not in the list")
            return
        }

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
                        items[itemId] = NewsItemState.ItemError(itemId, it)
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

    @Deprecated("Needs to be replaced with the new homepage viewmodel")
    suspend fun addToFavorites(itemId: Int, collection: UserDefinedItemCollection) {
        getItemById(itemId) // hack to make this function until we change everything to the new viewmodel
        // , and at this point we don't have the item in the cache here
            ?: throw IllegalStateException("Cannot add to favorites a non loaded item")
        val result = addToCollection(itemId, collection).onFailure {
            Timber.e(it, "Failed to add to collection")
        }
        if (result.isSuccess) {
            refreshItem(itemId)
        }
    }

    suspend fun removeFromFavorites(itemId: ItemId, collection: UserDefinedItemCollection) {
        getItemById(itemId) // hack to make this function until we change everything to the new viewmodel
        // , and at this point we don't have the item in the cache here
            ?: throw IllegalStateException("Cannot remove from favorites a non loaded item")
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
                items[id] ?: NewsItemState.Loading(id)
            }
        }

        if (outputList != null) {
            Timber.d("Updated List")
            _itemListFlow.emit(outputList)
        }
    }
}

class HomePageViewModel : ViewModel(), KoinComponent {

    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItemById: GetItemUseCase by inject()
    private val addToCollection: AddItemToCollectionUseCase by inject()
    private val removeFromCollection: RemoveItemFromCollectionUseCase by inject()
    private val refreshAllItems: RefreshAllItemsUseCase by inject()

    val collections = ALL_QUERIES.associateWith {
        ItemCollectionHolder(it,
            getNewStories,
            getItemById,
            addToCollection,
            removeFromCollection,
            refreshAllItems)
    }
}

class ItemCollectionHolder(
    val collection: ItemCollection,     // use cases
    private val getNewStories: GetNewStoriesUseCase,
    private val getItemById: GetItemUseCase,
    private val addToCollection: AddItemToCollectionUseCase,
    private val removeFromCollection: RemoveItemFromCollectionUseCase,
    private val refreshAllItems: RefreshAllItemsUseCase,
) {
    // internal state
    private val items: MutableMap<ItemId, NewsItemState> = ConcurrentHashMap()

    private val _pageState = MutableStateFlow<NewsPageState>(NewsPageState.Loading)
    val pageState = _pageState.asStateFlow()

    private val _itemListFlow: MutableSharedFlow<List<NewsItemState>> = MutableSharedFlow(1)
    val itemListFlow = _itemListFlow.asSharedFlow()

    suspend fun refreshAll() {
        refreshAllItems()
        loadAll()
    }

    suspend fun loadAll(retries: Int = 3) {
        synchronized(items) {
            items.clear()
        }
        _pageState.emit(NewsPageState.Loading)
        getNewStories(collection).fold(
            onSuccess = { itemsId ->
                synchronized(items) {
                    itemsId.forEach { id ->
                        items[id] = NewsItemState.Loading(id)
                    }
                }

                Timber.d("Loaded items $itemsId")

                _pageState.emit(NewsPageState.NewsIdsLoaded(itemsId))

                updateItemList()
            },
            onFailure = { itemsId ->
                synchronized(items) {


                }
            }
        )
    }


    suspend fun requestItem(itemId: Int, retries: Int = 3) {
        getItemById(itemId).fold(
            onSuccess = {
                synchronized(items) {
                    items[itemId] = NewsItemState.ItemLoaded(it)
                }
                updateItemList()
            },
            onFailure = { e ->
                when {
                    e is CancellationException -> {
                        // Coroutine is canceled, no need to worry
                    }
                    retries > 0 -> {
                        Timber.i("Failed to load $itemId due to ${e::class.simpleName}. Retrying other ${retries - 1} times")
                        requestItem(itemId, retries - 1)

                        Timber.d(e)

                    }
                    else -> {
                        Timber.w("Failed to load $itemId due to ${e::class.simpleName}. No more retries left.")
                        synchronized(items) {
                            items[itemId] = NewsItemState.ItemError(itemId, e)
                        }
                        updateItemList()
                    }
                }
            })
    }

    suspend fun addToFavorites(itemId: Int, collection: UserDefinedItemCollection) {
        getItemIfLoaded(itemId)
            ?: throw IllegalStateException("Cannot add to favorites a non loaded item")
        val result = addToCollection(itemId, collection).onFailure {
            Timber.e(it, "Failed to add to collection")
        }
        if (result.isSuccess) {
            requestItem(itemId)
        }
    }

    suspend fun removeFromFavorites(itemId: ItemId, collection: UserDefinedItemCollection) {
        getItemIfLoaded(itemId)
            ?: throw IllegalStateException("Cannot remove from favorites a non loaded item")
        val result = removeFromCollection(itemId, collection).onFailure {
            Timber.e(it, "Failed to remove from collection")
        }
        if (result.isSuccess) {
            requestItem(itemId)
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
                items[id] ?: NewsItemState.Loading(id)
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

sealed class NewsItemState(open val itemId: ItemId) {
    data class Loading(override val itemId: ItemId) : NewsItemState(itemId)
    data class ItemLoaded(val item: Item) : NewsItemState(item.id)
    data class ItemError(override val itemId: ItemId, val exception: Throwable) :
        NewsItemState(itemId)
}
