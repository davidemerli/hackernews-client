package it.devddk.hackernewsclient.pages.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
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
import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.pages.TabbedView
import it.devddk.hackernewsclient.pages.home.components.HNTopBar
import it.devddk.hackernewsclient.shared.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.shared.components.WebViewWithPrefs
import it.devddk.hackernewsclient.shared.components.news.NewsItem
import it.devddk.hackernewsclient.shared.components.news.SwipeableItem
import it.devddk.hackernewsclient.shared.components.topbars.ROUTE_ICONS
import it.devddk.hackernewsclient.shared.components.topbars.ROUTE_TITLES
import it.devddk.hackernewsclient.utils.ConnectionState
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.utils.connectivityState
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.ItemCollectionHolder
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalPagerApi::class
)
fun NewsPage(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    itemCollection: ItemCollection,
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)

    val coroutineScope = rememberCoroutineScope()

    val viewModel: HomePageViewModel = viewModel()
    val itemViewModel: SingleNewsViewModel = viewModel()

    val itemCollectionHolder = viewModel.collections[itemCollection]!!

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

    val webViewState = rememberWebViewState(if (readerMode) readabilityUrl else selectedItem?.url ?: "")
    val webViewInstance by itemViewModel.webView.collectAsState(initial = null)

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

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState
    ) {
        Scaffold(
            topBar = {
                HNTopBar(
                    title = ROUTE_TITLES[itemCollection::class.java.simpleName] ?: "route_name_error",
                    leadingIcon = ROUTE_ICONS[itemCollection::class.java.simpleName],
                    navController = navController,
                    drawerState = drawerState,
                    isOnArticle = isOnArticle,
                    selectedItem = selectedItem,
                    readerMode = readerMode,
                    darkMode = darkMode,
                    onClose = {
                        coroutineScope.launch {
                            itemViewModel.setId(null)
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
                    },
                    toggleCollection = { item, itemCollection ->
                        coroutineScope.launch {
                            viewModel.toggleFromCollection(item.id, itemCollection)
                        }
                    },
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
                    NewsExpandedLayout(
                        modifier = Modifier.padding(top = it.calculateTopPadding()),
                        navController = navController,
                        itemCollection = itemCollectionHolder,
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
                    NewsCompactLayout(
                        modifier = Modifier.padding(top = it.calculateTopPadding()),
                        navController = navController,
                        itemCollection = itemCollectionHolder,
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
}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun NewsExpandedLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    itemCollection: ItemCollectionHolder,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    selectedItem: Item?,
    expanded: Boolean = false,
    onExpandedClick: () -> Unit,
    webViewState: WebViewState,
    pagerState: PagerState,
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        if (!expanded) {
            NewsCompactLayout(
                navController = navController,
                selectedItem = null,
                itemCollection = itemCollection,
                onItemClick = onItemClick,
                onItemClickComments = onItemClickComments,
                webViewState = webViewState,
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

@ExperimentalCoroutinesApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun NewsCompactLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    itemCollection: ItemCollectionHolder,
    selectedItem: Item?,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    webViewState: WebViewState,
    pagerState: PagerState,
) {
    val connection by connectivityState(LocalContext.current)
    val isInternetAvailable = connection == ConnectionState.Available

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    val viewModel: HomePageViewModel = viewModel()
    val itemViewModel: SingleNewsViewModel = viewModel()
    val webViewInstance by itemViewModel.webView.collectAsState(initial = null)

    val itemListState = itemCollection.itemListFlow.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            itemCollection.loadAll()
        }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        refreshTriggerDistance = 144.dp,
        onRefresh = {
            if (selectedItem != null) {
                webViewInstance?.reload()
            } else {
                coroutineScope.launch {
                    viewModel.refreshAll()
                    itemCollection.loadAll()
                }
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
                if (!isInternetAvailable && itemCollection.collection !is UserDefinedItemCollection) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "You are not connected to the internet.\n\nOnly saved favorite and read later\nstories are available.",
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    itemsIndexed(itemListState.value, key = { _, item -> item.itemId }) { index, itemState ->
                        when (itemState) {
                            is NewsItemState.Loading, is NewsItemState.ItemError -> {
                                LaunchedEffect(index) {
                                    itemCollection.requestItem(itemState.itemId)
                                }

                                NewsItem(placeholder = true)
                            }
                            is NewsItemState.ItemLoaded -> {
                                SwipeableItem(
                                    item = itemState.item,
                                    onClick = { onItemClick(itemState.item) },
                                    onClickComments = { onItemClickComments(itemState.item) },
                                    onClickAuthor = { navController.navigate("user/${itemState.item.by}") },
                                    toggleCollection = { item, itemCollection ->
                                        coroutineScope.launch {
                                            viewModel.toggleFromCollection(item.id, itemCollection)
                                        }
                                    },
                                    placeholder = false
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

sealed class NewsPageRoutes

// TODO: refactor this?
data class HackerNewsView(val query: ItemCollection) : NewsPageRoutes() {
    val route: String = query::class.java.simpleName
}
