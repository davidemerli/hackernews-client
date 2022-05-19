package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SingleNewsViewModel : ViewModel(), KoinComponent {

    private val getItemById: GetItemUseCase by inject()

    private val _uiState = MutableStateFlow(SingleNewsUiState.Loading as SingleNewsUiState)
    val uiState = _uiState.asStateFlow()

    private val _commentsMap = MutableStateFlow(emptyMap<ItemId, CommentUiState>())
    val commentsMap = _commentsMap.asStateFlow()


    suspend fun setId(newId: Int?) {
        _commentsMap.value = emptyMap()
        _uiState.emit(SingleNewsUiState.Loading)
        if (newId == null) {
            _uiState.emit(SingleNewsUiState.Error(Exception("EEE")))
            return
        }
        withContext(Dispatchers.IO) {
            getItemById(newId).fold(onSuccess = {
                _uiState.emit(SingleNewsUiState.ItemLoaded(it))
            }, onFailure = {
                _uiState.emit(SingleNewsUiState.Error(it))
            })
        }
    }

    suspend fun expandComment(idToExpand: ItemId) {
        val currUiState = uiState.value
        if (currUiState is SingleNewsUiState.ItemLoaded) {
            when (val itemToExpand = _commentsMap.value[idToExpand]) {
                is CommentUiState.CommentLoaded -> loadSubcomments(itemToExpand.item)
                else -> { /* Do nothing */
                }
            }
        }
    }

    private suspend fun loadSubcomments(item: Item) {
        item.let { theItem ->
            withContext(Dispatchers.IO) {
                theItem.kids.forEach { id ->
                    getItemById(id).fold(onSuccess = { item ->
                        _commentsMap.update {
                            it + Pair(id, CommentUiState.CommentLoaded(item))
                        }
                    }, onFailure = { t ->
                        _commentsMap.update {
                            it + Pair(id, CommentUiState.Error(t))
                        }
                    })
                }
            }
        }
    }

    suspend fun getItem(id: ItemId, forceRefresh: Boolean = false) {
        if (forceRefresh || commentsMap.value[id] !is CommentUiState.CommentLoaded) {
            getItemById(id).fold(onSuccess = { item ->
                _commentsMap.update {
                    it + Pair(id, CommentUiState.CommentLoaded(item))
                }
            }, onFailure = { t ->
                _commentsMap.update {
                    it + Pair(id, CommentUiState.Error(t))
                }
            })
        }
    }
}


sealed class SingleNewsUiState {
    object Loading : SingleNewsUiState()
    data class Error(val throwable: Throwable) : SingleNewsUiState()
    data class ItemLoaded(val item: Item) : SingleNewsUiState()
}

sealed class CommentUiState {
    object Loading : CommentUiState()
    data class Error(val throwable: Throwable) : CommentUiState()
    data class CommentLoaded(val item: Item) : CommentUiState()
}