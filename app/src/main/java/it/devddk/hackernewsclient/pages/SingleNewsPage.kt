package it.devddk.hackernewsclient.pages

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.components.LinkifyText
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel


@Composable
@ExperimentalMaterial3Api
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

@Composable
@ExperimentalMaterial3Api
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
        LazyColumn(state = scrollState,
            modifier = Modifier.padding(top = it.calculateTopPadding())) {
            item {
                Text("${Html.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_COMPACT)}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.5.sp))
                // Don't display it if it is null
                item.text?.let { text ->
                    SelectionContainer {
                        LinkifyText("${Html.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Light,
                                lineHeight = 19.5.sp),
                            linkColor = MaterialTheme.colorScheme.secondary)
                    }
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
                    val commentState = comments.value.getOrDefault(it, CommentUiState.Loading)

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

    var expanded by rememberSaveable { mutableStateOf(false) }

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
    val depthColors: List<Color> = listOf(Color(0x66ef476f),
        Color(0x66ffd166),
        Color(0x6606d6a0),
        Color(0x66118ab2),
        Color(0x66073b4c))

    val context = LocalContext.current

    Row(modifier = Modifier
        .height(IntrinsicSize.Min)
        .padding(start = ((depth + 1) * 6).dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))) {
        Box(modifier = Modifier
            .width(10.dp)
            .fillMaxHeight()
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            .background(depthColors[depth % depthColors.size]))

        when (commentState) {
            is CommentUiState.CommentLoaded -> {
                val text = commentState.item.text?.let { text ->
                    Html.fromHtml(text, HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS)
                } ?: "< deleted comment >"

                val timeString = remember(commentState.item) {
                    TimeDisplayUtils(context).toDateTimeAgoInterval(commentState.item.time)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = buildAnnotatedString {
                            pushStyle(SpanStyle(textDecoration = TextDecoration.Underline,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold))
                            append(commentState.item.by ?: "")
                            pop()
                            append(" • ")
                            append(timeString)
                        },
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    SelectionContainer {
                        LinkifyText(
                            "${text.trim()}",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            linkColor = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    val numKids = commentState.item.kids.size

                    if (numKids > 0) {
                        Box(modifier = Modifier
                            .height(48.dp)
                            .padding(vertical = 4.dp)
                            .clickable {
                                onClick()
                            }) {
                            Row(modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("$numKids comments",
                                    style = MaterialTheme.typography.bodySmall)
                                Icon(if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    if (expanded) "Close Comments" else "Open Comments")
                            }
                        }
                    }
                }

            }
            is CommentUiState.Error -> Text("< ERROR >",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth())

            is CommentUiState.Loading -> Text("LOADING ...",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth())
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