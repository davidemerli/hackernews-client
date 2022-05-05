package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.model.utils.TopStories
import it.devddk.hackernewsclient.domain.utils.skipped
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.collections.LinkedHashMap

class HomePageViewModel : ViewModel(), KoinComponent {

    companion object {
        const val DEFAULT_REQUEST_UNTIL = 20
    }

    private var ids : Result<List<ItemId>>? = null

    private val query : MutableStateFlow<CollectionQueryType> = MutableStateFlow(TopStories)

    private val loadUntil = MutableStateFlow(0)
    private val _uiMessages = MutableStateFlow(ListUpdateMessage.ClearAll as ListUpdateMessage)
    private val uiMessage = _uiMessages.asStateFlow()
    private val itemStorage = MutableStateFlow(emptyMap<Int, MessageState>())
    val shownList : Flow<List<MessageState>> = itemStorage.map { map ->
        (0 until loadUntil.value).map { i -> map.getOrDefault(i, MessageState.Loading) }
    }

    private val getNewStories: GetNewStoriesUseCase by inject()
    private val getItem: GetItemUseCase by inject()


    init {
        viewModelScope.launch {
            uiMessage.onEach { msg ->
                when(msg) {
                    is ListUpdateMessage.AddItem -> {
                        itemStorage.update { old -> old + Pair(msg.index, msg.item.fold(
                            onSuccess = { MessageState.ItemLoaded(it)},
                            onFailure = { MessageState.Loading}
                        ))}
                    }
                    is ListUpdateMessage.ClearAll -> {
                        itemStorage.update { emptyMap() }
                    }
                }
            }.collect()
        }
    }


    suspend fun setQuery(newQuery : CollectionQueryType) {
        val oldQuery = query.value
        if(oldQuery != newQuery) {
            query.emit(newQuery)
        }
    }

    suspend fun requestMessage(newUntil : Int) {
        if((ids?.isSuccess != true)) {
            refreshItemIds()
        }
        if(newUntil > loadUntil.value) {
            (loadUntil.value..newUntil).forEach { index ->
                val id = getIdFromIndex(index)
                if(id != null) {
                    _uiMessages.emit(ListUpdateMessage.AddItem(index, getItem(id)))
                } else  {
                    _uiMessages.emit(ListUpdateMessage.AddItem(index, Result.skipped()))
                }
            }
        }
    }

    suspend fun requestItem(newUntil : Int) {
        if(ids?.isFailure != false) {
            refreshItemIds()
        }
        if(newUntil > loadUntil.value) {
            (loadUntil.value..newUntil).forEach { index ->
                getIdFromIndex(index)?.let { id ->
                    _uiMessages.emit(ListUpdateMessage.AddItem(index, getItem(id)))
                } ?: _uiMessages.emit(ListUpdateMessage.AddItem(index, Result.skipped()))
            }
        }
    }

    private fun getIdFromIndex(index : Int) : ItemId? {
        return ids?.getOrNull()?.getOrNull(index)
    }

    private suspend fun refreshItemIds() {
        ids = getNewStories(query.value)
        loadUntil.value = 0
        _uiMessages.emit(ListUpdateMessage.ClearAll)
    }
}


sealed class MessageState {
    object Loading : MessageState()
    data class ItemLoaded(val item : Item) : MessageState()
}

sealed class ListUpdateMessage {
    object ClearAll : ListUpdateMessage()
    data class AddItem(val index : Int, val item : Result<Item>) : ListUpdateMessage()
}

