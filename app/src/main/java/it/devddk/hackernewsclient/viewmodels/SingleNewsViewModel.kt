package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import it.devddk.hackernewsclient.domain.interaction.item.GetCommentTreeUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemTree
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.utils.Stack
import it.devddk.hackernewsclient.domain.utils.pop
import it.devddk.hackernewsclient.domain.utils.push
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.FieldPosition
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.exp

class SingleNewsViewModel : ViewModel(), KoinComponent {

    private val getCommentTree: GetCommentTreeUseCase by inject()

    private val _uiState = MutableStateFlow(SingleNewsUiState.Loading as SingleNewsUiState)

    private val _linearizedTree = mutableListOf<CommentTreeUiState>()
    private val _commentTreeFlow : MutableSharedFlow<List<CommentTreeUiState>> = MutableSharedFlow(1)
    val commentTreeFlow = _commentTreeFlow.asSharedFlow()


    val uiState = _uiState.asStateFlow()

    suspend fun setId(newId: Int?) {
        Timber.d("Set article $newId")
        _uiState.emit(SingleNewsUiState.Loading)
        if (newId == null) {
            _uiState.emit(SingleNewsUiState.Error(Exception("EEE")))
            return
        }
        withContext(Dispatchers.IO) {
            getCommentTree(newId).fold(
                onSuccess = { tree ->
                    _uiState.emit(SingleNewsUiState.ItemLoaded(tree.item))
                    val newTree = tree.mapToUiState {depth -> depth <= 1}.dfsLinearize()
                    synchronized(_linearizedTree) {
                        _linearizedTree.clear()
                        _linearizedTree.addAll(newTree)
                    }
                },
                onFailure = {
                    _uiState.emit(SingleNewsUiState.Error(it))
                    _linearizedTree.clear()
                }
            )
        }
    }

    suspend fun setExpansion(position: Int, expanded: Boolean) {
        synchronized(_linearizedTree) {
            _linearizedTree[position].setExpansion(expanded)
        }
        notifyListUpdated()
    }

    private suspend fun notifyListUpdated() {
        _commentTreeFlow.emit(_linearizedTree)
    }
}


sealed class SingleNewsUiState {
    object Loading : SingleNewsUiState()
    data class Error(val throwable: Throwable) : SingleNewsUiState()
    data class ItemLoaded(val rootItem: Item) :
        SingleNewsUiState()
}

class CommentTreeUiState(
    val item: Item,
    val children: List<CommentTreeUiState>,
    val depth: Int,
    expanded: Boolean,
    visible: Boolean,
) {

    var expanded: Boolean = expanded
    private set
    var visible: Boolean = visible
    private set

    fun setExpansion(newExpansion: Boolean) {
        if (expanded == newExpansion) {
            return
        }
        expanded = newExpansion
        children.forEach {
            setVisibility(newExpansion)
        }
    }


    fun setVisibility(newVisibility: Boolean) {
        if (visible == newVisibility) {
            return
        }
        visible = newVisibility
        if(expanded) {
            children.forEach {
                it.setVisibility(newVisibility)
            }
        }
    }

    fun dfsLinearize(): MutableList<CommentTreeUiState> {
        val stack = Stack<CommentTreeUiState>()
        val list = mutableListOf<CommentTreeUiState>()
        stack.push(this)
        while (stack.isNotEmpty()) {
            val nextNode = stack.pop()!!

            if (nextNode.depth >= 1) {
                list.add(nextNode)
            }
            nextNode.children.reversed().forEach {
                stack.push(it)
            }
        }
        return list
    }
}

fun ItemTree.mapToUiState(
    depth: Int = 0,
    initialDepthF: (depth: Int) -> Boolean,
): CommentTreeUiState {
    return CommentTreeUiState(
        item,
        comments.map { it.mapToUiState(depth + 1, initialDepthF) },
        depth,
        expanded = initialDepthF(depth + 1),
        visible = initialDepthF(depth))
}