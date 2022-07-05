package it.devddk.hackernewsclient.pages.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.collection.BestStories
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.pages.TabbedView
import it.devddk.hackernewsclient.pages.home.components.GoToLocationRow
import it.devddk.hackernewsclient.pages.home.components.HNTopBar
import it.devddk.hackernewsclient.pages.home.components.MediumNewsRow
import it.devddk.hackernewsclient.pages.home.components.NewsColumn
import it.devddk.hackernewsclient.pages.home.components.TallNewsRow
import it.devddk.hackernewsclient.shared.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.ItemCollectionHolder
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomePage(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)

    val coroutineScope = rememberCoroutineScope()

    val viewModel: HomePageViewModel = viewModel()
    val itemViewModel: SingleNewsViewModel = viewModel()

    val bestCollection = viewModel.collections[BestStories]!!
    val topCollection = viewModel.collections[TopStories]!!
    val readLaterCollection = viewModel.collections[UserDefinedItemCollection.ReadLater]!!

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

    HNModalNavigatorPanel(navController = navController, state = drawerState) {
        Scaffold(
            topBar = {
                HNTopBar(
                    navController = navController,
                    drawerState = drawerState,
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
                    }
                )
            },
        ) {
            when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Expanded -> {
                    ExpandedLayout(
                        modifier = Modifier.padding(top = it.calculateTopPadding()),
                        navController = navController,
                        bestCollection = bestCollection,
                        topCollection = topCollection,
                        readLaterCollection = readLaterCollection,
                        onItemClick = { item -> onItemClick(item) },
                        onItemClickComments = { item -> onItemClickComments(item) },
                        selectedItem = selectedItem,
                        expanded = expandedArticleView,
                        onExpandedClick = { expandedArticleView = !expandedArticleView },
                        webViewState = webViewState,
                    )
                }
                WindowWidthSizeClass.Compact,
                WindowWidthSizeClass.Medium,
                -> {
                    CompactLayout(
                        modifier = Modifier.padding(top = it.calculateTopPadding()),
                        navController = navController,
                        bestCollection = bestCollection,
                        topCollection = topCollection,
                        readLaterCollection = readLaterCollection,
                        selectedItem = selectedItem,
                        onItemClick = { item -> onItemClick(item) },
                        onItemClickComments = { item -> onItemClickComments(item) },
                        webViewState = webViewState,
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandedLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    bestCollection: ItemCollectionHolder,
    topCollection: ItemCollectionHolder,
    readLaterCollection: ItemCollectionHolder,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    selectedItem: Item?,
    expanded: Boolean = false,
    onExpandedClick: () -> Unit,
    webViewState: WebViewState,
) {
    val viewModel: SingleNewsViewModel = viewModel()

    Row(
        modifier = modifier.fillMaxSize()
    ) {
        if (!expanded) {
            CompactLayout(
                navController = navController,
                bestCollection = bestCollection,
                topCollection = topCollection,
                readLaterCollection = readLaterCollection,
                selectedItem = null,
                onItemClick = onItemClick,
                onItemClickComments = onItemClickComments,
                webViewState = webViewState,
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .fillMaxHeight()
            )
        }

        if (selectedItem != null) {
            LaunchedEffect(selectedItem) { viewModel.setId(selectedItem.id) }

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
fun CompactLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    bestCollection: ItemCollectionHolder,
    topCollection: ItemCollectionHolder,
    readLaterCollection: ItemCollectionHolder,
    selectedItem: Item?,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    webViewState: WebViewState
) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val readLaterItems = readLaterCollection.itemListFlow.collectAsState(initial = emptyList())
    val showReadLater by derivedStateOf { readLaterItems.value.isNotEmpty() }

    val viewModel: HomePageViewModel = viewModel()
    val itemViewModel: SingleNewsViewModel = viewModel()
    val webViewInstance by itemViewModel.webView.collectAsState(initial = null)

    LaunchedEffect(readLaterCollection) {
        readLaterCollection.loadAll()
    }

    SwipeRefresh(
        state = swipeRefreshState,
        refreshTriggerDistance = 144.dp,
        onRefresh = {
            if (selectedItem != null) {
                webViewInstance?.reload()
            } else {
                coroutineScope.launch { bestCollection.refreshAll() }
                coroutineScope.launch { topCollection.refreshAll() }
                coroutineScope.launch { readLaterCollection.refreshAll() }
            }
        }
    ) {
        if (selectedItem != null) {
            LaunchedEffect(selectedItem) { itemViewModel.setId(selectedItem.id) }

            TabbedView(
                navController = navController,
                item = selectedItem,
                webViewState = webViewState,
                modifier = modifier
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
            ) {
                GoToLocationRow(
                    leadingIcon = Icons.Filled.AutoAwesome,
                    location = "Best Stories",
                    buttonText = "See more",
                    onClick = {
                        navController.navigate("BestStories")
                    }
                )

                TallNewsRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    itemCollection = bestCollection,
                    onItemClick = onItemClick,
                    onItemClickComments = onItemClickComments,
                )

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.1f)
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                )

                if (showReadLater) {
                    GoToLocationRow(
                        leadingIcon = Icons.Filled.Bookmark,
                        location = "Your saves",
                        buttonText = "See more", onClick = {
                            navController.navigate("ReadLater")
                        }
                    )

                    MediumNewsRow(
                        itemCollection = readLaterCollection,
                        onItemClickComments = onItemClickComments,
                        onItemClick = onItemClick,
                    )

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(0.1f)
                            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                    )
                }

                GoToLocationRow(
                    leadingIcon = Icons.Filled.TrendingUp,
                    location = "Top Stories",
                    buttonText = "See more",
                    onClick = {
                        navController.navigate("TopStories")
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                NewsColumn(
                    itemCollection = topCollection,
                    onItemClick = onItemClick,
                    toggleCollection = { item, itemCollection ->
                        coroutineScope.launch {
                            viewModel.toggleFromCollection(item.id, itemCollection)

                            // reload if item is added to read later in order to update the view
                            if (itemCollection is UserDefinedItemCollection.ReadLater) {
                                readLaterCollection.refreshAll()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 384.dp),
                )

                GoToLocationRow(
                    buttonText = "Back to top",
                    actionIcon = Icons.Filled.ArrowUpward,
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
