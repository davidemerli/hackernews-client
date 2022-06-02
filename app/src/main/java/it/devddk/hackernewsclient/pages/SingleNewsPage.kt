package it.devddk.hackernewsclient.pages

import android.annotation.SuppressLint
import android.text.Html
import android.text.Spanned
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.components.LinkifyText
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlin.math.absoluteValue
import kotlin.math.max


@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
fun SingleNewsPage(navController: NavController, id: Int?) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val uiState = mViewModel.uiState.collectAsState(SingleNewsUiState.Loading)

    LaunchedEffect(id) {
        mViewModel.setId(id)
    }

    when (val uiStateValue = uiState.value) {
        is SingleNewsUiState.Error -> Error(throwable = uiStateValue.throwable)
        is SingleNewsUiState.ItemLoaded -> TabbedView(uiStateValue.item, navController)
        SingleNewsUiState.Loading -> Loading()
    }
}

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@OptIn(ExperimentalPagerApi::class)
fun TabbedView(item: Item, navController: NavController) {
    var tabPosition by remember { mutableStateOf(0) }
    val tabs = listOf(
        "comments",
        "article",
    )
    var tabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Article", "Comments")
    val pagerState = rememberPagerState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },

                navigationIcon = {
                    Icon(Icons.Rounded.Menu, "Menu")
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Rounded.AccountCircle, "Notifications")
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        it.calculateTopPadding()
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
        ) {
            TabRow(selectedTabIndex = tabIndex,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.pagerTabIndicatorOffset(
                            pagerState,
                            tabPositions,
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(text = title, color = MaterialTheme.colorScheme.secondary) }
                    )
                }
            }

            HorizontalPager(
                count = tabTitles.size,
                state = pagerState,
                itemSpacing = 0.dp,
                verticalAlignment = Alignment.Top,
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> Article(item)
                    1 -> Comments(item)
                }
            }
        }
    }
}

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@SuppressLint("SetJavaScriptEnabled")
fun Article(item: Item) {
    item.url?.let { url ->
        val viewState = rememberWebViewState(url = url)

        if (viewState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Using a lazycol avoids scrolling problems when in horizontal pager

        LazyColumn {
            item {
                WebView(
                    state = viewState,
                    onCreated = {
                        it.loadUrl(item.url ?: "")
                        it.settings.javaScriptEnabled = true
                        it.settings.domStorageEnabled = true
                    }
                )
            }
        }

    }
}

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
fun Comments(item: Item) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val scrollState = rememberLazyListState()
    val comments = mViewModel.commentList.collectAsState(emptyList())

    LazyColumn(state = scrollState) {
        item {
            Text("${Html.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_COMPACT)}",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.5.sp))
            // Don't display it if it is null
            item.text?.let { text ->
                SelectionContainer {
                    LinkifyText(
                        text.toSpanned().toString(),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Light,
                            lineHeight = 19.5.sp
                        ),
                        linkColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }


        itemsIndexed(comments.value) { _, thisComment ->
            LaunchedEffect(thisComment.itemId) {
                mViewModel.getItem(thisComment.itemId)
            }

            ExpandableComment(
                thisComment,
                rootItem = item,
            )
        }
    }
}

fun Item.isExpandable() = kids.isNotEmpty()


@Composable
@ExperimentalComposeUiApi
fun ExpandableComment(
    comment: CommentUiState,
    rootItem: Item,
) {
    val mViewModel: SingleNewsViewModel = viewModel()
    val expandable =
        comment is CommentUiState.CommentLoaded && comment.item.isExpandable()

    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        CommentCard(comment,
            expanded = expanded,
            rootItem = rootItem,
            onClick = {
                expanded = !expanded
            }
        )

        LaunchedEffect(comment.itemId, expanded) {
            mViewModel.expandComment(comment.itemId, expandable && expanded)
        }
    }
}

@Composable
@ExperimentalComposeUiApi
fun CommentCard(
    commentState: CommentUiState,
    rootItem: Item,
    onClick: () -> Unit,
    expanded: Boolean,
) {
    val depthColors: List<Color> = integerArrayResource(id = R.array.depth_colors).map {
        Color(it)
    }
    val depth = commentState.depth

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
                val text = commentState.item.text ?: "< deleted comment >"

                val timeString = remember(commentState.item) {
                    TimeDisplayUtils(context).toDateTimeAgoInterval(commentState.item.time)
                }

                val isOriginalPoster = rootItem.by == commentState.item.by

                val byString =
                    "${commentState.item.by}${if (isOriginalPoster) " (OP)" else ""}"

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
                            alpha = 1f)
                    )

                    SelectionContainer {
                        LinkifyText(
                            text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT).toString(),
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Visible,
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
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    pluralStringResource(R.plurals.comments,
                                        numKids,
                                        numKids
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Icon(
                                    if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    if (expanded) stringResource(R.string.close_comments) else stringResource(
                                        R.string.open_comments)
                                )
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

@ExperimentalPagerApi
fun Modifier.pagerTabIndicatorOffset(
    pagerState: PagerState,
    tabPositions: List<TabPosition>,
): Modifier = composed {
    // If there are no pages, nothing to show
    if (pagerState.pageCount == 0) return@composed this

    val targetIndicatorOffset: Dp
    val indicatorWidth: Dp

    val currentTab = tabPositions[minOf(tabPositions.lastIndex, pagerState.currentPage)]
    val targetPage = pagerState.targetPage
    val targetTab = tabPositions.getOrNull(targetPage)

    if (targetTab != null) {
        // The distance between the target and current page. If the pager is animating over many
        // items this could be > 1
        val targetDistance = (targetPage - pagerState.currentPage).absoluteValue
        // Our normalized fraction over the target distance
        val fraction = (pagerState.currentPageOffset / max(targetDistance, 1)).absoluteValue

        targetIndicatorOffset = lerp(currentTab.left, targetTab.left, fraction)
        indicatorWidth = lerp(currentTab.width, targetTab.width, fraction).absoluteValue
    } else {
        // Otherwise we just use the current tab/page
        targetIndicatorOffset = currentTab.left
        indicatorWidth = currentTab.width
    }

    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = targetIndicatorOffset)
        .width(indicatorWidth)
}

private inline val Dp.absoluteValue: Dp
    get() = value.absoluteValue.dp


fun isFromYCombinator(url: String): Boolean {
    return url.matches("https://news\\.ycombinator\\.com/item\\?id=\\d{8}".toRegex())
}

fun String.toSpanned(): Spanned {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
}
