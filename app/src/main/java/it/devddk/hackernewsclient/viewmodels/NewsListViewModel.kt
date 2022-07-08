package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import it.devddk.hackernewsclient.domain.interaction.collection.AddItemToCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.collection.GetCollectionsUseCase
import it.devddk.hackernewsclient.domain.interaction.collection.RemoveItemFromCollectionUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.interaction.item.RefreshAllItemsUseCase
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.collection.UserStories
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class HomePageViewModel : ViewModel(), KoinComponent {

    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItemById: GetItemUseCase by inject()
    private val getCollectionsByItem: GetCollectionsUseCase by inject()
    private val addToCollection: AddItemToCollectionUseCase by inject()
    private val removeFromCollection: RemoveItemFromCollectionUseCase by inject()
    private val refreshAllItems: RefreshAllItemsUseCase by inject()

    val collections = ALL_QUERIES.associateWith {
        ItemCollectionHolder(
            it,
            getNewStories,
            getItemById,
            addToCollection,
            removeFromCollection
        )
    }

    fun userCollection(userStories: UserStories): ItemCollectionHolder {
        return ItemCollectionHolder(
            userStories,
            getNewStories,
            getItemById,
            addToCollection,
            removeFromCollection,
        )
    }

    suspend fun toggleFromCollection(
        itemId: Int,
        collection: UserDefinedItemCollection,
    ): Result<Boolean> {
        return getCollectionsByItem(itemId).mapCatching { collections ->
            if (!collections.contains(collection)) {
                addToCollection(itemId, collection).onFailure {
                    Timber.e(
                        it,
                        "Failed to add to collection"
                    )
                }
            } else {
                removeFromCollection(itemId, collection).onFailure {
                    Timber.e(
                        it,
                        "Failed to remove to collection"
                    )
                }
            }
            !collections.contains(collection)
        }
    }

    suspend fun refreshAll() {
        refreshAllItems()
    }
}

class ItemCollectionHolder(
    val collection: ItemCollection, // use cases
    private val getNewStories: GetNewStoriesUseCase,
    private val getItemById: GetItemUseCase,
    private val addToCollection: AddItemToCollectionUseCase,
    private val removeFromCollection: RemoveItemFromCollectionUseCase,
) {
    // internal state
    private val items: MutableMap<ItemId, NewsItemState> = ConcurrentHashMap()

    private val _pageState = MutableStateFlow<NewsPageState>(NewsPageState.Loading)
    val pageState = _pageState.asStateFlow()

    private val _itemListFlow: MutableSharedFlow<List<NewsItemState>> = MutableSharedFlow(1)
    val itemListFlow = _itemListFlow.asSharedFlow()

    suspend fun loadAll() {
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
            onFailure = {
                Timber.e(it, "Failed to load items")
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
            }
        )
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

    fun getItemIfLoaded(itemId: Int): Item? {
        synchronized(items) {
            return (items[itemId] as? NewsItemState.ItemLoaded)?.item
        }
    }

    private suspend fun updateItemList() {
        val outputList: List<NewsItemState>?
        synchronized(items) {
            val itemState = pageState.value as? NewsPageState.NewsIdsLoaded
            outputList = itemState?.itemIds?.map { id ->
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
    data class NewsIdsLoaded(val itemIds: List<ItemId>) : NewsPageState()
    data class NewsIdsError(val exception: Throwable) : NewsPageState()
}

sealed class NewsItemState(open val itemId: ItemId) {
    data class Loading(override val itemId: ItemId) : NewsItemState(itemId)
    data class ItemLoaded(val item: Item) : NewsItemState(item.id)
    data class ItemError(override val itemId: ItemId, val exception: Throwable) :
        NewsItemState(itemId)
}
