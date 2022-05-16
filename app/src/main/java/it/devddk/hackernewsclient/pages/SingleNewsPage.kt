package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch


@Composable
fun SingleNewsPage(navController: NavController, id: Int?) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val uiState = mViewModel.uiState.collectAsState(SingleNewsUiState.Loading)

    LaunchedEffect(id) {
        mViewModel.setId(id)
    }

    when (val uiStateValue = uiState.value) {
        is SingleNewsUiState.Error -> Error(throwable = uiStateValue.throwable)
        is SingleNewsUiState.ItemLoaded -> Comments(uiStateValue.item)
        SingleNewsUiState.Loading -> Loading()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comments(item: Item) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val scrollState = rememberLazyListState()
    val comments = mViewModel.commentsMap.collectAsState()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            navigationIcon = {
                Icon(Icons.Rounded.Menu, "Menu")
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.Search, "Search")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.AccountCircle, "Notifications")
                }
            },
        )
    }, containerColor = MaterialTheme.colorScheme.background) {
        LazyColumn(state = scrollState) {
            item {
                Text(
                    "${item.title}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.5.sp
                    )
                )
                // Don't display it if it is null
                if (item.text != null) {
                    Text("${item.text}")
                }
            }

            items(item.kids.size) { index ->
                val thisComment = item.kids.getOrNull(index)
                LaunchedEffect(index) {
                    thisComment?.let {
                        mViewModel.getItem(it)
                    }
                }
                thisComment?.let {
                    val commentState =
                        comments.value.getOrDefault(it, CommentUiState.Loading)
                    ExpandableComment(commentState, comments.value, 0)
                }
            }
        }
    }
}

fun Item.isExpandable() = kids.isNotEmpty()


@Composable
fun ExpandableComment(parentComment: CommentUiState, comments: Map<ItemId, CommentUiState>, depth: Int) {
    val mViewModel: SingleNewsViewModel = viewModel()
    var expanded by remember { mutableStateOf(false) }
    val expandable =
        parentComment is CommentUiState.CommentLoaded && parentComment.item.isExpandable()

    Column {
        CommentCard(parentComment, depth, onClick = {
            if (expandable) {
                expanded = !expanded
            }
        })
        if (expandable && expanded) {
            val commentItem = (parentComment as CommentUiState.CommentLoaded).item
            commentItem.kids.forEach { childId ->
                LaunchedEffect(childId) {
                    mViewModel.getItem(childId)
                }
                ExpandableComment(comments.getOrDefault(childId, CommentUiState.Loading), comments, depth + 1)
            }
        }
    }
}

@Composable
fun CommentCard(commentState: CommentUiState, depth: Int, onClick : () -> Unit) {
    Box(Modifier.clickable {
        onClick()
    }) {
        when (commentState) {
            is CommentUiState.CommentLoaded -> Text("($depth) ${commentState.item.text ?: "Peppe"}",
                modifier = Modifier.padding(15.dp))
            is CommentUiState.Error -> Text("Errorrrrr",
                modifier = Modifier.padding(15.dp))
            is CommentUiState.Loading -> Text("LOADING",
                modifier = Modifier.padding(15.dp))
        }
    }

}


@Composable
fun Error(throwable: Throwable) {
    Text("Error")
}

@Composable
fun Loading() {
    Text("Loading")
}