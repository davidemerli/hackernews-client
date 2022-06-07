package it.devddk.hackernewsclient.pages

import android.annotation.SuppressLint
import android.text.Html
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.components.*
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch

fun String.toSpanned(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()
}

@Composable
fun SingleNewsPage(navController: NavController, id: Int?) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val uiState = mViewModel.uiState.collectAsState(SingleNewsUiState.Loading)

    LaunchedEffect(id) {
        mViewModel.setId(id)
    }

    when (val uiStateValue = uiState.value) {
        is SingleNewsUiState.Error -> {
            Error(throwable = uiStateValue.throwable)
        }
        is SingleNewsUiState.Loading -> {
            Loading()
        }
        is SingleNewsUiState.ItemLoaded -> {
            TabbedView(uiStateValue.item, navController)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun TabbedView(item: Item, navController: NavController) {
    var tabIndex by remember { mutableStateOf(0) }
    //TODO: remove article when item.url is null and remove horizontal paging
    val tabs = listOf("Article", "Comments")

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollState = rememberLazyListState()

    val mViewModel: SingleNewsViewModel = viewModel()
    val comments = mViewModel.commentList.collectAsState(emptyList())

    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        SmallTopAppBar(
            scrollBehavior = scrollBehavior,
            modifier = Modifier.wrapContentHeight(Alignment.Bottom),
            title = { },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.AccountCircle, "Notifications")
                }
            },
        )
    }, containerColor = MaterialTheme.colorScheme.background) {
        LazyColumn(state = scrollState,
            modifier = Modifier.padding(top = it.calculateTopPadding())) {
            // TODO: replace with horizontal pager
            stickyHeader {
                TabRow(selectedTabIndex = tabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = {
                            Text(text = title, color = MaterialTheme.colorScheme.secondary)
                        })
                    }
                }
            }

            when (tabIndex) {
                0 -> {
                    item { ArticleView(item) }
                }
                1 -> {
                    item {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            ItemDomain(item = item)
                            ItemTitle(item = item)
                            Row {
                                ItemBy(item)

                                Text(text = " - ")

                                ItemTime(item)
                            }
                        }

                        ArticleDescription(item = item)
                    }

                    itemsIndexed(comments.value, key = { _, item -> item.itemId }) { _, comment ->
                        Box(modifier = Modifier.animateItemPlacement()) {
                            if (comment !is CommentUiState.CommentLoaded) {
                                LaunchedEffect(comment.itemId) {
                                    mViewModel.getItem(comment.itemId)
                                }
                            }

                            ExpandableComment(
                                comment,
                                rootItem = item,
                            )
                        }
                    }

                    item {
                        Text("< end of comments >",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary,
                            ),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
@SuppressLint("SetJavaScriptEnabled")
fun ArticleView(item: Item) {
    item.url?.let { url ->
        val viewState = rememberWebViewState(url = url)

        if (viewState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary)
        }

        // Using a lazycol avoids scrolling problems when in horizontal pager

        WebView(state = viewState, onCreated = {
            it.loadUrl(item.url ?: "")
            it.settings.javaScriptEnabled = true
            it.settings.domStorageEnabled = true
        })
    }
}

@Composable
fun ArticleDescription(item: Item) {
    if (!item.text.isNullOrBlank()) {
        SelectionContainer {
            LinkifyText(item.text!!.toSpanned(),
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Light,
                    lineHeight = 19.5.sp),
                linkColor = MaterialTheme.colorScheme.secondary)
        }
    }
}

fun Item.isExpandable() = kids.isNotEmpty()
fun CommentUiState.isExpandable() = this is CommentUiState.CommentLoaded && this.item.isExpandable()

@Composable
fun ExpandableComment(
    comment: CommentUiState,
    rootItem: Item,
) {
    val mViewModel: SingleNewsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var expanded by rememberSaveable { mutableStateOf(false) }

    val onClick = {
        if (comment.isExpandable()) {
            expanded = !expanded

            coroutineScope.launch {
                mViewModel.expandComment(comment.itemId,
                    comment.isExpandable() && expanded)
            }
        }
    }

    when (comment) {
        is CommentUiState.CommentLoaded -> {
            CommentCard(
                comment.item,
                comment.depth,
                expanded = expanded,
                rootItem = rootItem,
                onClick = onClick
            )
        }
        is CommentUiState.Error -> Text("< ERROR >",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth())
        //TODO: use placeholder CommentCard
        is CommentUiState.Loading -> Text("LOADING ...",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth())
    }
}

@Composable
fun DepthIndicator(depth: Int) {
    val depthColors: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }

    Box(modifier = Modifier
        .width(10.dp)
        .fillMaxHeight()
        .padding(end = 4.dp)
        .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
        .background(depthColors[depth % depthColors.size]))
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun ExpandChildren(expanded: Boolean, numKids: Int, onClick: () -> Unit) {
    Box(modifier = Modifier
        .height(48.dp)
        .padding(vertical = 4.dp)
        .clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(pluralStringResource(R.plurals.comments, numKids, numKids),
                style = MaterialTheme.typography.bodySmall)

            Icon(if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                if (expanded) stringResource(R.string.close_comments) else stringResource(R.string.open_comments))
        }
    }
}

//TODO: define placeholders modifiers and a placeholder comment
@Composable
fun CommentCard(
    item: Item,
    depth: Int = 0,
    rootItem: Item,
    expanded: Boolean,
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current

    val paddingStart = (depth * 6).dp + 2.dp

    val depthColors: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }

    // obtains a background color for the comments which is a slight tint of the colorScheme secondary color
    val commentBackground = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.background.toArgb(),
        0.9f))

    Row(modifier = Modifier
        .height(IntrinsicSize.Min)
        .padding(start = paddingStart, top = 4.dp, end = 4.dp, bottom = 4.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(commentBackground)) {
        DepthIndicator(depth = depth)

        val text = item.text ?: "< deleted comment >"

        val timeString = remember(item) {
            TimeDisplayUtils(context).toDateTimeAgoInterval(item.time)
        }

        val isOriginalPoster = rootItem.by == item.by

        val byString = "${item.by}${if (isOriginalPoster) " (OP)" else ""}"

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = buildAnnotatedString {
                pushStyle(SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    fontStyle = if (isOriginalPoster) FontStyle.Normal else FontStyle.Italic,
                    fontWeight = if (isOriginalPoster) FontWeight.Black else FontWeight.SemiBold,
                ))
                append(byString)
                pop()
                append(" • ")
                append(timeString)
            },
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isOriginalPoster) MaterialTheme.colorScheme.primary else depthColors[depth % depthColors.size].copy(
                    alpha = 1f))

            SelectionContainer {
                LinkifyText(
                    text = text.toSpanned(),
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Visible,
                    linkColor = MaterialTheme.colorScheme.secondary,
                )
            }

            val numKids = item.kids.size

            if (numKids > 0) {
                ExpandChildren(expanded = expanded, numKids = numKids, onClick = onClick)
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

fun isFromYCombinator(url: String): Boolean {
    return url.matches("https://news\\.ycombinator\\.com/item\\?id=\\d{8}".toRegex())
}
