package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.components.ArticleView
import it.devddk.hackernewsclient.components.CommentText
import it.devddk.hackernewsclient.components.ExpandableComment
import it.devddk.hackernewsclient.components.ItemBy
import it.devddk.hackernewsclient.components.ItemDomain
import it.devddk.hackernewsclient.components.ItemTime
import it.devddk.hackernewsclient.components.ItemTitle
import it.devddk.hackernewsclient.components.SingleNewsPageTopBar
import it.devddk.hackernewsclient.components.WebViewWithPrefs
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.DEFAULT_PREFERRED_VIEW
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max

fun String.toSpanned(): String {
    return HtmlCompat.fromHtml(
        this,
        HtmlCompat.FROM_HTML_MODE_LEGACY
    ).toString().trim()
}

@Composable
fun SingleNewsPage(navController: NavController, id: Int?, selectedView: String? = null) {

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
            TabbedView(uiStateValue.item, navController, selectedView)
        }
    }
}

@Composable
fun SingleNewsPage(navController: NavController, item: Item, selectedView: String? = null) {
    val viewModel: SingleNewsViewModel = viewModel()

    LaunchedEffect(item.id) {
        viewModel.setId(item.id)
    }

    TabbedView(item = item, navController = navController, selectedView)
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
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class,
)
fun TabbedView(item: Item, navController: NavController, selectedView: String?) {
    val tabs = item.url?.let { listOf("Article", "Comments (${item.descendants ?: 0})") } ?: listOf(
        "Comments (${item.descendants ?: 0})"
    )

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    var fullScreenWebView by remember { mutableStateOf(false) }
    val webviewState = rememberWebViewState(url = item.url ?: "")

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

    Scaffold(
        topBar = {
            if (!fullScreenWebView) {
                SingleNewsPageTopBar(item, navController)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (tabs[pagerState.currentPage] == "Article") {
                FloatingActionButton(
                    onClick = { fullScreenWebView = !fullScreenWebView },
                ) {
                    Icon(
                        if (fullScreenWebView) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "Fullscreen"
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.padding(top = it.calculateTopPadding())) {
            if (item.url != null) {
                if (fullScreenWebView) {
                    WebViewWithPrefs(webviewState, false)
                } else {
                    item.url?.let {
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
                                        pagerState.animateScrollToPage(index)
                                    }
                                }, text = {
                                    Text(text = title, color = MaterialTheme.colorScheme.secondary)
                                })
                            }
                        }
                    }
                    HorizontalPager(
                        count = item.url?.let { 2 } ?: 1,
                        state = pagerState,
                        modifier = Modifier.fillMaxHeight(),
                        userScrollEnabled = !fullScreenWebView
                    ) { index ->
                        when (index) {
                            0 -> {
                                ArticleView(item, webviewState)
                            }
                            1 -> {
                                CommentsView(item, navController = navController)
                            }
                        }
                    }
                }
            } else {
                CommentsView(item, navController = navController)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
fun CommentsView(item: Item, navController: NavController) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)
    val depthSize = dataStore.depth.collectAsState(initial = SettingPrefs.DEFAULT_DEPTH)

    val mViewModel: SingleNewsViewModel = viewModel()
    val comments = mViewModel.commentList.collectAsState(emptyList())

    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
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
                ItemDomain(item = item)
                ItemTitle(item = item)
                Row {
                    ItemBy(item = item)

                    Text(text = " - ")

                    ItemTime(item = item)
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
