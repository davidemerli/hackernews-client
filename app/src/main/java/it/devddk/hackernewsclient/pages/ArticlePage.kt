package it.devddk.hackernewsclient.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.shared.components.ArticleView
import it.devddk.hackernewsclient.shared.components.CommentText
import it.devddk.hackernewsclient.shared.components.ExpandableComment
import it.devddk.hackernewsclient.shared.components.WebViewWithPrefs
import it.devddk.hackernewsclient.shared.components.news.NewsItemAuthor
import it.devddk.hackernewsclient.shared.components.news.NewsItemDomain
import it.devddk.hackernewsclient.shared.components.news.NewsItemTime
import it.devddk.hackernewsclient.shared.components.news.NewsItemTitle
import it.devddk.hackernewsclient.shared.components.news.getDomainName
import it.devddk.hackernewsclient.shared.components.topbars.SingleNewsPageTopBar
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.DEFAULT_PREFERRED_VIEW
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.max

fun String.parseHTML(): String {
    return HtmlCompat.fromHtml(
        this,
        HtmlCompat.FROM_HTML_MODE_LEGACY
    ).toString().trim()
}

@Composable
fun ArticlePage(
    navController: NavController,
    windowWidthSizeClass: WindowWidthSizeClass,
    id: Int?,
    selectedView: String? = null,
) {

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
            ArticlePage(
                item = uiStateValue.item,
                navController = navController,
                windowWidthSizeClass = windowWidthSizeClass,
                selectedView = selectedView,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
fun ArticlePage(
    navController: NavController,
    windowWidthSizeClass: WindowWidthSizeClass,
    item: Item,
    selectedView: String? = null,
) {
    val viewModel: SingleNewsViewModel = viewModel()
    val webviewState = rememberWebViewState(item.url ?: "")

    val scrollState = rememberLazyListState()

    LaunchedEffect(item.id) {
        viewModel.setId(item.id)
    }

    when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact,
        WindowWidthSizeClass.Medium,
        -> {
            TabbedView(
                item = item,
                navController = navController,
                webViewState = webviewState,
                selectedView = selectedView,
            )
        }
        WindowWidthSizeClass.Expanded -> {
            Scaffold(
                topBar = {
                    SingleNewsPageTopBar(
                        item = item,
                        navController = navController,
                    )
                },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = it.calculateTopPadding())
                ) {
                    ArticleView(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        webviewState = webviewState
                    )

                    CommentsView(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        navController = navController,
                        scrollState = scrollState
                    )
                }
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

@Composable
@OptIn(ExperimentalPagerApi::class, ExperimentalComposeUiApi::class)
fun TabbedView(
    modifier: Modifier = Modifier,
    item: Item,
    navController: NavController,
    webViewState: WebViewState,
    pagerState: PagerState = rememberPagerState(),
    selectedView: String? = null
) {
    val tabs = item.url?.let { listOf("Article", "Comments (${item.descendants ?: 0})") } ?: listOf(
        "Comments (${item.descendants ?: 0})"
    )

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    var fullScreenWebView by remember { mutableStateOf(false) }

    val dataStore = SettingPrefs(LocalContext.current)
    val preferredView = selectedView
        ?: dataStore.preferredView.collectAsState(initial = DEFAULT_PREFERRED_VIEW).value

    if (tabs.size > 1) {
        LaunchedEffect(preferredView) {
            coroutineScope.launch {
                pagerState.scrollToPage(if (preferredView == "article") 0 else 1)
            }
        }
    }

    BackHandler(enabled = fullScreenWebView, onBack = { fullScreenWebView = false })

    BoxWithConstraints {
        val showAdjacent = maxWidth > 800.dp

        Column(
            modifier = modifier,
        ) {
            if (item.url != null) {
                if (webViewState.isLoading) {
                    LinearProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                AnimatedVisibility(visible = !fullScreenWebView && !showAdjacent) {
                    val tabSizePx = this@BoxWithConstraints.maxWidth.value *
                        LocalContext.current.resources.displayMetrics.density

                    BoxWithConstraints {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.pagerTabIndicatorOffset(
                                        pagerState,
                                        tabPositions,
                                    ),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(selected = pagerState.currentPage == index, onClick = {
                                    coroutineScope.launch {
                                        // FIXME: this refreshes the webview, idk if fixable
                                        pagerState.animateScrollBy(if (index == 0) -tabSizePx else tabSizePx)
                                    }
                                }, text = {
                                    Text(text = title, color = MaterialTheme.colorScheme.secondary)
                                })
                            }
                        }
                    }
                }

                var userScroll by remember { mutableStateOf(true) }

                if (!showAdjacent) {
                    HorizontalPager(
                        count = item.url?.let { 2 } ?: 1,
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        userScrollEnabled = !fullScreenWebView and userScroll,
                    ) { index ->
                        when (index) {
                            0 -> {
                                Box(
                                    modifier = Modifier.clickable {
                                        Timber.d("owo")
                                    }
                                ) {
                                    WebViewWithPrefs(
                                        state = webViewState,
                                        setScroll = { value -> userScroll = value },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                            1 -> {
                                CommentsView(
                                    item = item,
                                    navController = navController,
                                    scrollState = scrollState,
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WebViewWithPrefs(
                            state = webViewState,
                            modifier = Modifier.fillMaxWidth(0.55f)
                        )

                        CommentsView(
                            item = item,
                            navController = navController,
                            scrollState = scrollState,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                CommentsView(
                    item = item,
                    navController = navController,
                    scrollState = scrollState,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
fun CommentsView(
    modifier: Modifier = Modifier,
    navController: NavController,
    item: Item,
    scrollState: LazyListState,
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)
    val depthSize = dataStore.depth.collectAsState(initial = SettingPrefs.DEFAULT_DEPTH)

    val mViewModel: SingleNewsViewModel = viewModel()
    val comments = mViewModel.commentList.collectAsState(emptyList())

    val domain = remember { item.url?.let { getDomainName(it) } }

    LazyColumn(
        state = scrollState,
        modifier = modifier.fillMaxSize()
        // .scrollbar(
        //    scrollState,
        //    trackColor = MaterialTheme.colorScheme.surfaceVariant,
        //    knobColor = MaterialTheme.colorScheme.onSurfaceVariant,
        // )
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight()
            ) {
                if (domain != null) {
                    NewsItemDomain(domain = domain)
                }
                item.title?.let { NewsItemTitle(title = it) }
                Row {
                    NewsItemAuthor(author = item.by)

                    Text(text = " - ")

                    item.time?.let { NewsItemTime(time = it) }
                }
                ArticleDescription(item = item)
            }
        }

        itemsIndexed(comments.value, key = { _, item -> item.itemId }) { _, comment ->
            Box(modifier = Modifier.animateItemPlacement()) {
                if (comment !is CommentUiState.CommentLoaded) {
                    LaunchedEffect(comment.itemId) {
                        mViewModel.requestItem(comment.itemId)
                    }
                }

                ExpandableComment(
                    comment = comment,
                    rootItem = item,
                    depthSize = depthSize.value.toInt(),
                    listState = scrollState,
                    navController = navController,
                )
            }
        }

        item {
            Text(
                "< end of comments >",
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ArticleDescription(item: Item) {
    if (!item.text.isNullOrBlank()) {
        CommentText(item = item)
    }
}

fun isFromYCombinator(url: String): Boolean {
    return url.matches("https://news\\.ycombinator\\.com/item\\?id=\\d{8}".toRegex())
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
