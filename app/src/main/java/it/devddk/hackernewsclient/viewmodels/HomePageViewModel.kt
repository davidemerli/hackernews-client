package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.*
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.utils.TopStories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    private var ids: Result<List<ItemId>>? = null

    private val query: MutableStateFlow<CollectionQueryType> = MutableStateFlow(TopStories)

    private val loadUntil = MutableStateFlow(0)
    private val itemStorage = MutableStateFlow(emptyMap<Int, ItemState>())
    val shownList: Flow<List<ItemState>> = itemStorage.map { map ->
        (0 until query.value.maxAmount).map { i -> map.getOrDefault(i, ItemState.Loading) }
    }
    private val _uiMessages = MutableSharedFlow<ListUpdateMessage>()
    private val uiMessages = _uiMessages.asSharedFlow()



    init {
        viewModelScope.launch {
            uiMessages.onEach { msg ->
                when (msg) {

                    is ListUpdateMessage.AddItem -> {
                        Timber.d("Appending item ${msg.index}")
                        itemStorage.update { old ->
                            old + Pair(msg.index, msg.item)
                        }
                    }
                    is ListUpdateMessage.ClearAll -> {
                        Timber.d("Clearing all items")
                        itemStorage.update { emptyMap() }
                    }
                }
            }.collect()
        }
    }

    suspend fun setQuery(newQuery: CollectionQueryType) {
        val oldQuery = query.value
        if (oldQuery != newQuery) {
            query.emit(newQuery)
        }
    }

    suspend fun requestMore(newUntil: Int) {

        if ((ids?.isSuccess != true)) {
            refreshItemIds()
        }
        if (newUntil > loadUntil.value) {

            withContext(Dispatchers.IO) {
                val oldUntil = loadUntil.getAndUpdate {
                    newUntil
                }
                loadUntil.value = newUntil
                Timber.d("Loading $oldUntil $newUntil")
                (oldUntil..newUntil).forEach { index ->
                    if (itemStorage.value[index] !is ItemState.ItemLoaded) {
                        val id = getIdFromIndex(index)
                        if (id != null) {
                            withContext(Dispatchers.IO) {
                                _uiMessages.emit(ListUpdateMessage.AddItem(index, getItem(id).fold(
                                    onSuccess = {
                                        Timber.d("Got item $index")
                                        ItemState.ItemLoaded(it)
                                    },
                                    onFailure = {
                                        ItemState.ItemError(it)
                                    }
                                )))
                            }
                        }
                    }
                }
            }

        }
    }


    private fun getIdFromIndex(index: Int): ItemId? {
        return ids?.getOrNull()?.getOrNull(index)
    }

    private suspend fun refreshItemIds() {
        ids = getNewStories(query.value)
        loadUntil.value = 0
        _uiMessages.emit(ListUpdateMessage.ClearAll)
    }
}


sealed class ItemState {
    object Loading : ItemState()
    data class ItemLoaded(val item: Item) : ItemState()
    data class ItemError(val exception: Throwable) : ItemState()
}

sealed class ListUpdateMessage {
    object ClearAll : ListUpdateMessage()
    data class AddItem(val index: Int, val item: ItemState) : ListUpdateMessage()
}

