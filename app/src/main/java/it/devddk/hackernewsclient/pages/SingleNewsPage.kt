package it.devddk.hackernewsclient.pages

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel


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

    val expandedItems = remember { mutableSetOf<ItemId>() }

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
        LazyColumn(
            state = scrollState,
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
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
                    thisComment?.let { it ->
                        mViewModel.getItem(it)
                    }
                }
                thisComment?.let { it ->
                    val commentState =
                        comments.value.getOrDefault(it, CommentUiState.Loading)

                    ExpandableComment(
                        commentState,
                        comments.value,
                        0,
                    )
                }
            }
        }
    }
}

fun Item.isExpandable() = kids.isNotEmpty()


@Composable
fun ExpandableComment(
    parentComment: CommentUiState,
    comments: Map<ItemId, CommentUiState>,
    depth: Int,
) {
    val mViewModel: SingleNewsViewModel = viewModel()
    val expandable =
        parentComment is CommentUiState.CommentLoaded && parentComment.item.isExpandable()

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        CommentCard(parentComment, depth, onClick = {
            expanded = !expanded
        }, expanded = expanded)

        if (expandable && expanded) {
            val commentItem = (parentComment as CommentUiState.CommentLoaded).item
            commentItem.kids.forEach { childId ->
                LaunchedEffect(childId) {
                    mViewModel.getItem(childId)
                }
                ExpandableComment(comments.getOrDefault(childId, CommentUiState.Loading),
                    comments,
                    depth + 1)
            }
        }
    }
}

@Composable
fun CommentCard(commentState: CommentUiState, depth: Int, onClick: () -> Unit, expanded: Boolean) {
    val depthColors: List<Color> = listOf(
        Color(0xffef476f),
        Color(0xffffd166),
        Color(0xff06d6a0),
        Color(0xff118ab2),
        Color(0xff073b4c)
    )

    val context = LocalContext.current

    Box(Modifier.clickable {
        onClick()
    }) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = (depth * 6).dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(depthColors[depth % depthColors.size])
            )

            when (commentState) {
                is CommentUiState.CommentLoaded -> {
                    val text = commentState.item.text?.let { text ->
                        Html.fromHtml(text, HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS)
                    } ?: "< deleted comment >"

                    val timeString = remember(commentState.item) {
                        TimeDisplayUtils(context).toDateTimeAgoInterval(commentState.item.time)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "${commentState.item.by} - $timeString",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                                fontStyle = FontStyle.Italic
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,

                            )

                        Text(
                            "${text.trim()}",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val numKids = commentState.item.kids.size

                        if (numKids > 0) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("" +
                                        "$numKids comments",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Icon(
                                    if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    if (expanded) "Close Comments" else "Open Comments"
                                )
                            }
                        }
                    }

                }
                is CommentUiState.Error -> Text("Errorrrrr",
                    modifier = Modifier.padding(15.dp))
                is CommentUiState.Loading -> Text("LOADING",
                    modifier = Modifier.padding(15.dp))
            }
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