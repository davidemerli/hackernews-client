package it.devddk.hackernewsclient.viewmodels

import androidx.lifecycle.ViewModel
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.domain.utils.Stack
import it.devddk.hackernewsclient.domain.utils.pop
import it.devddk.hackernewsclient.domain.utils.push
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class SingleNewsViewModel : ViewModel(), KoinComponent {

    // internal state
    private val mCommentsMap: MutableMap<ItemId, CommentUiState> = ConcurrentHashMap()

    // use cases
    private val getItemById: GetItemUseCase by inject()

    // state flows
    private val _uiState = MutableStateFlow(SingleNewsUiState.Loading as SingleNewsUiState)
    val uiState = _uiState.asStateFlow()

    private val _commentList = MutableSharedFlow<List<CommentUiState>>(1)
    val commentList = _commentList.asSharedFlow()

    private fun dfs(root: ItemId, map: Map<ItemId, CommentUiState>): List<CommentUiState> {
        val result: MutableList<CommentUiState> = mutableListOf()
        val stack: Stack<ItemId> = mutableListOf()
        stack.push(root)
        while (stack.isNotEmpty()) {
            val nextId = stack.pop()!!
            val nextComment = map[nextId] ?: continue
            if (nextComment.depth >= 0) {
                result.add(nextComment)
            }
            if (nextComment is CommentUiState.CommentLoaded && nextComment.expanded) {
                nextComment.item.kids.reversed().forEach { kidId ->
                    stack.push(kidId)
                }
            }
        }
        return result
    }

    // TODO: we can avoid completely this since we already have all information regarding the article
    // and this results in having additional useless loading time when clicking on an article
    suspend fun setId(newId: Int?) {
        Timber.d("Set article $newId")
        _uiState.emit(SingleNewsUiState.Loading)

        if (newId == null) {
            _uiState.emit(SingleNewsUiState.Error(Exception("EEE")))
            return
        }

        withContext(Dispatchers.IO) {
            getItemById(newId).fold(
                onSuccess = { item ->
                    _uiState.emit(SingleNewsUiState.ItemLoaded(item))

                    synchronized(mCommentsMap) {
                        mCommentsMap.clear()
                        mCommentsMap[newId] = CommentUiState.CommentLoaded(item, -1, true)

                        item.kids.forEach { kidId ->
                            mCommentsMap[kidId] = CommentUiState.Loading(kidId, 0)
                        }
                    }
                    updateCommentList()
                },
                onFailure = {
                    _uiState.emit(SingleNewsUiState.Error(it))
                }
            )
        }
    }

    suspend fun expandComment(idToExpand: Int, expanded: Boolean) {
        // Timber.v("${if (expanded) "E" else "Une"}xpand item $idToExpand")
        val rebuildTree: Boolean

        synchronized(mCommentsMap) {
            val commentToExpand = mCommentsMap[idToExpand]

            if (commentToExpand !is CommentUiState.CommentLoaded) {
                return@expandComment
            }

            when {
                commentToExpand.expanded == expanded -> {
                    rebuildTree = false
                }

                expanded -> {
                    rebuildTree = true

                    commentToExpand.item.kids.forEach { kidId ->
                        if (mCommentsMap[kidId] !is CommentUiState.CommentLoaded) {
                            mCommentsMap[kidId] = CommentUiState.Loading(
                                itemId = kidId,
                                depth = commentToExpand.depth + 1
                            )
                        }
                    }

                    mCommentsMap[idToExpand] = commentToExpand.copy(expanded = true)
                }

                else -> {
                    rebuildTree = true

                    mCommentsMap[idToExpand] = commentToExpand.copy(expanded = false)
                }
            }
        }

        if (rebuildTree) {
            updateCommentList()
        }
    }

    suspend fun requestItem(id: ItemId, forceRefresh: Boolean = false) {
        val commentState = mCommentsMap[id]!!

        if (forceRefresh || commentState !is CommentUiState.CommentLoaded) {
            withContext(Dispatchers.IO) {
                getItemById(id).fold(onSuccess = { item ->
                    mCommentsMap[id] = CommentUiState.CommentLoaded(
                        item = item,
                        depth = commentState.depth,
                        expanded = false
                    )
                }, onFailure = { t ->
                    mCommentsMap[id] = CommentUiState.Error(
                        itemId = id,
                        throwable = t,
                        depth = commentState.depth
                    )
                })
            }

            updateCommentList()
        }
    }

    private suspend fun updateCommentList() {
        Timber.d("Rebuild tree")

        when (val currUiState = uiState.value) {
            is SingleNewsUiState.ItemLoaded -> {
                val result: List<CommentUiState>

                synchronized(mCommentsMap) {
                    result = dfs(currUiState.item.id, mCommentsMap)
                }

                _commentList.emit(result)
            }
            else -> {}
        }
    }
}

sealed class SingleNewsUiState {
    object Loading : SingleNewsUiState()

    data class Error(
        val throwable: Throwable
    ) : SingleNewsUiState()

    data class ItemLoaded(
        val item: Item
    ) : SingleNewsUiState()
}

sealed class CommentUiState(open val itemId: ItemId, open val depth: Int) {
    data class Loading(
        override val itemId: ItemId,
        override val depth: Int
    ) :
        CommentUiState(itemId, depth)

    data class Error(
        override val itemId: ItemId,
        val throwable: Throwable,
        override val depth: Int,
    ) :
        CommentUiState(itemId, depth)

    data class CommentLoaded(
        val item: Item,
        override val depth: Int,
        val expanded: Boolean
    ) :
        CommentUiState(item.id, depth)
}
