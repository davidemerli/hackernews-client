package it.devddk.hackernewsclient.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.search.SearchResult
import it.devddk.hackernewsclient.pages.home.components.HNTopBar
import it.devddk.hackernewsclient.shared.components.WebViewWithPrefs
import it.devddk.hackernewsclient.shared.components.news.SwipeableItem
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.SearchPageViewModel
import it.devddk.hackernewsclient.viewmodels.SearchResultUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalPagerApi::class
)
fun SearchPage(
    navController: NavController,
    query: String? = null,
    windowSizeClass: WindowSizeClass
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)

    val coroutineScope = rememberCoroutineScope()

    val itemViewModel: SingleNewsViewModel = viewModel()

    val itemUiState by itemViewModel.uiState.collectAsState(initial = SingleNewsUiState.Loading)

    val selectedItem by derivedStateOf {
        if (itemUiState is SingleNewsUiState.ItemLoaded) {
            (itemUiState as SingleNewsUiState.ItemLoaded).item
        } else null
    }

    var expandedArticleView by rememberSaveable { mutableStateOf(false) }
    var readerMode by remember { mutableStateOf(false) }
    val darkMode by dataStore.darkMode.collectAsState(initial = SettingPrefs.DEFAULT_DARK_MODE)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val onItemClick = { item: Item ->
        coroutineScope.launch {
            itemViewModel.setId(item.id)
            readerMode = false
        }
    }

    val onItemClickComments = { item: Item ->
        coroutineScope.launch {
            itemViewModel.setId(item.id) // TODO: go to comments in tabbed
            readerMode = false
        }
    }

    val readabilityUrl = "https://readability.davidemerli.com?convert=${selectedItem?.url ?: ""}"

    val webViewState =
        rememberWebViewState(if (readerMode) readabilityUrl else selectedItem?.url ?: "")
    val webViewInstance by itemViewModel.webView.collectAsState(initial = null)

    var searchQuery by rememberSaveable { mutableStateOf(query) }
    val viewModel: SearchPageViewModel = viewModel()

    val homePageViewModel: HomePageViewModel = viewModel()

    if (searchQuery != null) {
        LaunchedEffect(searchQuery) {
            if (searchQuery!!.length >= 3) {
                viewModel.updateSimpleQuery(searchQuery!!)
            }
        }
    }

    BackHandler(enabled = selectedItem != null, onBack = {
        coroutineScope.launch {
            itemViewModel.setId(null)
        }

        readerMode = false
        expandedArticleView = false
    })

    var openDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState()

    val isOnArticle by derivedStateOf {
        expandedArticleView || (pagerState.currentPage == 0 && pagerState.pageCount == 2)
    }

    Scaffold(
        topBar = {
            HNTopBar(
                title = searchQuery ?: "Search",
                query = searchQuery,
                focusable = true,
                navController = navController,
                drawerState = drawerState,
                isOnArticle = isOnArticle,
                selectedItem = selectedItem,
                readerMode = readerMode,
                darkMode = darkMode,
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        itemViewModel.setId(null)
                    }
                },
                toggleCollection = { item, itemCollection ->
                    coroutineScope.launch {
                        homePageViewModel.toggleFromCollection(item.id, itemCollection)
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
                        webViewInstance?.loadUrl(selectedItem?.url ?: "")
                    }
                }
            )
        },
        floatingActionButtonPosition = if (expandedArticleView) FabPosition.Center else FabPosition.End,
        floatingActionButton = {
            if (selectedItem != null && pagerState.currentPage == 0 && pagerState.pageCount == 2) {
                FloatingActionButton(
                    onClick = { openDialog = true }
                ) {
                    Icon(Icons.Filled.Fullscreen, "Expand")
                }
            }
        }
    ) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> {
                SearchExpandedLayout(
                    modifier = Modifier.padding(top = it.calculateTopPadding()),
                    navController = navController,
                    onItemClick = { item -> onItemClick(item) },
                    onItemClickComments = { item -> onItemClickComments(item) },
                    selectedItem = selectedItem,
                    expanded = expandedArticleView,
                    onExpandedClick = { expandedArticleView = !expandedArticleView },
                    webViewState = webViewState,
                    pagerState = pagerState,
                )
            }
            WindowWidthSizeClass.Compact,
            WindowWidthSizeClass.Medium -> {
                SearchCompactLayout(
                    modifier = Modifier.padding(top = it.calculateTopPadding()),
                    navController = navController,
                    selectedItem = selectedItem,
                    onItemClick = { item -> onItemClick(item) },
                    onItemClickComments = { item -> onItemClickComments(item) },
                    webViewState = webViewState,
                    pagerState = pagerState,
                )
            }
        }

        if (openDialog) {
            Dialog(
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = { openDialog = false },
            ) {
                WebViewWithPrefs(
                    modifier = Modifier.fillMaxSize(),
                    state = webViewState,
                    verticalScrollState = null
                )
            }
        }
    }
}

@Composable
fun ResultItem(
    result: SearchResult,
    onClick: () -> Unit = {},
    onClickComments: () -> Unit = {},
    toggleCollection: (Item, UserDefinedItemCollection) -> Unit = { _, _ -> },
) {
    Timber.d("ResultItem: ${result.item.by} - ${result.item.type}")

    // TODO: differentiate onClick

    when (result.item.type) {
        ItemType.COMMENT,
        ItemType.STORY,
        ItemType.JOB,
        ItemType.POLL -> {
            SwipeableItem(
                item = result.item,
                searchMetaData = result.searchMetaData,
                onClick = onClick,
                onClickComments = onClickComments,
                toggleCollection = toggleCollection,
                placeholder = false
            )
        }
        else -> {
            Text(result.item.id.toString())
        }
    }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun SearchExpandedLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    selectedItem: Item?,
    expanded: Boolean = false,
    onExpandedClick: () -> Unit,
    webViewState: WebViewState,
    pagerState: PagerState,
    prefixContent: @Composable () -> Unit = { }
) {

    Row(
        modifier = modifier.fillMaxSize()
    ) {
        if (!expanded) {
            SearchCompactLayout(
                navController = navController,
                selectedItem = null,
                onItemClick = onItemClick,
                onItemClickComments = onItemClickComments,
                webViewState = webViewState,
                prefixContent = prefixContent,
                pagerState = pagerState,
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .fillMaxHeight()
            )
        }

        if (selectedItem != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(4.dp)
            ) {
                Icon(
                    if (expanded) Icons.Filled.ChevronRight else Icons.Filled.ChevronLeft,
                    contentDescription = if (!expanded) "Expand View" else "Collapse View",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.primary)
                        .size(28.dp)
                        .clickable { onExpandedClick() }
                )
            }

            TabbedView(
                navController = navController,
                item = selectedItem,
                webViewState = webViewState,
                pagerState = pagerState,
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_item_selected),
                    modifier = Modifier.alpha(0.5f)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun SearchCompactLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    selectedItem: Item?,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    webViewState: WebViewState,
    pagerState: PagerState,
    prefixContent: @Composable () -> Unit = { }
) {
    val coroutineScope = rememberCoroutineScope()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val scrollState = rememberLazyListState()

    val homePageViewModel: HomePageViewModel = viewModel()

    val itemViewModel: SingleNewsViewModel = viewModel()
    val webViewInstance by itemViewModel.webView.collectAsState(initial = null)

    val viewModel: SearchPageViewModel = viewModel()
    val resultList = viewModel.resultListFlow.collectAsState(initial = emptyList())

    SwipeRefresh(
        state = swipeRefreshState,
        refreshTriggerDistance = 144.dp,
        onRefresh = {
            if (selectedItem != null) {
                webViewInstance?.reload()
            } else {
                // TODO: define reload function in viewmodel
            }
        }
    ) {
        if (selectedItem != null) {
            TabbedView(
                navController = navController,
                item = selectedItem,
                webViewState = webViewState,
                pagerState = pagerState,
                modifier = modifier
            )
        } else {
            LazyColumn(
                state = scrollState,
                modifier = modifier.fillMaxSize()
            ) {
                item { prefixContent() }

                items(resultList.value.size + 1) { index ->
                    LaunchedEffect(index.div(20)) {
                        viewModel.requestItem(index)
                    }

                    when (val result = resultList.value.getOrNull(index)) {
                        is SearchResultUiState.ResultLoaded -> {
                            ResultItem(
                                result.result,
                                onClick = { onItemClick(result.result.item) },
                                onClickComments = { onItemClickComments(result.result.item) },
                                toggleCollection = { item, collection ->
                                    coroutineScope.launch {
                                        homePageViewModel.toggleFromCollection(item.id, collection)
                                    }
                                }
                            )
                        }
                        else -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
