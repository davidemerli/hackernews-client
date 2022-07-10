package it.devddk.hackernewsclient.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
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
import it.devddk.hackernewsclient.pages.home.components.HNTopBar
import it.devddk.hackernewsclient.shared.components.CommentText
import it.devddk.hackernewsclient.shared.components.ExpandableComment
import it.devddk.hackernewsclient.shared.components.WebViewWithPrefs
import it.devddk.hackernewsclient.shared.components.news.NewsItemAuthor
import it.devddk.hackernewsclient.shared.components.news.NewsItemDomain
import it.devddk.hackernewsclient.shared.components.news.NewsItemTime
import it.devddk.hackernewsclient.shared.components.news.NewsItemTitle
import it.devddk.hackernewsclient.shared.components.news.getDomainName
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
    id: Int?, // FIXME: this should not be able to be null
    selectedView: String? = null,
) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val uiState = mViewModel.uiState.collectAsState(SingleNewsUiState.Loading)

    LaunchedEffect(Unit) {
        mViewModel.setId(id)
    }

    when (val uiStateValue = uiState.value) {
        is SingleNewsUiState.Error -> {
            Error(
                throwable = uiStateValue.throwable,
                onFeedbackClick = {
                    navController.navigate("feedback/$id")
                }
            )
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
                viewModelSetupDone = true
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
    viewModelSetupDone: Boolean = false,
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)

    val darkMode by dataStore.darkMode.collectAsState(initial = SettingPrefs.DEFAULT_DARK_MODE)
    var readerMode by rememberSaveable { mutableStateOf(false) }

    val viewModel: SingleNewsViewModel = viewModel()

    val webViewState = rememberWebViewState(item.url ?: "")
    val webViewInstance by viewModel.webView.collectAsState(initial = null)

    val pagerState = rememberPagerState()

    val scrollState = rememberLazyListState()

    val activity = LocalContext.current as? Activity

    val coroutineScope = rememberCoroutineScope()

    if (!viewModelSetupDone) {
        LaunchedEffect(item.id) {
            viewModel.setId(item.id)
        }
    }

    val readabilityUrl = "https://readability.davidemerli.com?convert=${item.url ?: ""}"

    val isOnArticle by derivedStateOf {
        WindowWidthSizeClass.Expanded == windowWidthSizeClass || (pagerState.currentPage == 0 && pagerState.pageCount == 2)
    }

    Scaffold(
        topBar = {
            HNTopBar(
                navController = navController,
                selectedItem = item,
                isOnArticle = isOnArticle,
                readerMode = readerMode,
                darkMode = darkMode,
                onClose = {
                    if (!navController.popBackStack()) {
                        activity?.finish()
                    }
                },
                onDarkModeClick = {
                    coroutineScope.launch { dataStore.setDarkMode(!darkMode) }
                },
                onReaderModeClick = {
                    readerMode = !readerMode

                    if (readerMode) {
                        webViewInstance?.loadUrl(readabilityUrl)
                    } else {
                        webViewInstance?.loadUrl(item.url ?: "")
                    }
                }
            )
        },
    ) {
        when (windowWidthSizeClass) {
            WindowWidthSizeClass.Compact,
            WindowWidthSizeClass.Medium -> {
                TabbedView(
                    item = item,
                    navController = navController,
                    pagerState = pagerState,
                    webViewState = webViewState,
                    selectedView = selectedView,
                    modifier = Modifier.padding(top = it.calculateTopPadding())
                )
            }
            WindowWidthSizeClass.Expanded -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = it.calculateTopPadding())
                ) {
                    WebViewWithPrefs(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        state = webViewState
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
fun Error(
    throwable: Throwable,
    onFeedbackClick: () -> (Unit) = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Unfortunately we could not load this item.")

        TextField(
            value = throwable.message ?: "Error",
            onValueChange = {},
            readOnly = true,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(256.dp)
                .padding(top = 8.dp)
        )

        Button(
            onClick = onFeedbackClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(Icons.Filled.Feedback, "Feedback")
            Text("Report Error", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun Loading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun TabbedView(
    modifier: Modifier = Modifier,
    item: Item,
    navController: NavController,
    webViewState: WebViewState,
    pagerState: PagerState = rememberPagerState(),
    selectedView: String? = null
) {
    Timber.d("TabbedView: selectedView: $selectedView")

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
    val fontSize = dataStore.fontSize.collectAsState(initial = MaterialTheme.typography.bodyLarge.fontSize.value)

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
                    NewsItemAuthor(
                        author = item.by,
                        onClick = {
                            navController.navigate("user/${item.by}")
                        }
                    )

                    Text(text = " - ")

                    item.time?.let { NewsItemTime(time = it) }
                }
                ArticleDescription(item = item)
            }
        }

        // if the item is a comment we display a button to go to its root story
        if (item.storyId != null && item.storyId != item.id) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Posted on: ", modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "${item.storyTitle ?: item.storyId}", // TODO: story title
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic
                        ),
                    )

                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = {
                        navController.navigate("items/${item.storyId}")
                    }) {
                        Text("Go to story")

                        Icon(Icons.Filled.KeyboardArrowRight, "Arrow Right")
                    }
                }
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
                    fontSize = fontSize.value.sp,
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
    val dataStore = SettingPrefs(LocalContext.current)
    val fontSize = dataStore.fontSize.collectAsState(initial = MaterialTheme.typography.bodyLarge.fontSize.value)

    if (!item.text.isNullOrBlank()) {
        CommentText(item = item, fontSize = fontSize.value.sp, modifier = Modifier.fillMaxWidth())
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
