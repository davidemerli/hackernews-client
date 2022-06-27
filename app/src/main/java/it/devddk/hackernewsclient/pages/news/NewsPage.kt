package it.devddk.hackernewsclient.pages.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.pages.ArticlePage
import it.devddk.hackernewsclient.shared.components.news.NewsItem
import it.devddk.hackernewsclient.shared.components.news.SwipeableNewsItem
import it.devddk.hackernewsclient.shared.components.topbars.FeedbackButton
import it.devddk.hackernewsclient.shared.components.topbars.HNModalNavigatorPanel
import it.devddk.hackernewsclient.shared.components.topbars.HomePageTopBar
import it.devddk.hackernewsclient.shared.components.topbars.OpenInBrowserButton
import it.devddk.hackernewsclient.shared.components.topbars.SearchButton
import it.devddk.hackernewsclient.shared.components.topbars.ShareButton
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.NewsPageState
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
fun NewsPage(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    route: ItemCollection,
) {
    val viewModel: HomePageViewModel = viewModel()

    val itemCollection = viewModel.collections[route]!!
    val pageState = itemCollection.pageState.collectAsState(NewsPageState.Loading)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val query = viewModel.currentQuery.collectAsState(initial = TopStories)

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    val onClick = { item: Item ->
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            selectedItem = item
        } else {
            navController.navigate("items/${item.id}")
        }
    }

    val onClickComments = { item: Item ->
        navController.navigate("items/${item.id}/comments")
    }

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState,
        query = "TopStories",
    ) {
        Scaffold(
            topBar = {
                HomePageTopBar(
                    navController = navController,
                    state = drawerState,
                    query = "TopStories",
                    actions = {
                        SearchButton(navController = navController)

                        if (selectedItem != null) {
                            ShareButton(
                                itemUrl = selectedItem!!.url,
                                hnUrl = "https://news.ycombinator.com/item?id=${selectedItem!!.id}"
                            )

                            OpenInBrowserButton(
                                itemUrl = selectedItem!!.url,
                                hnUrl = "https://news.ycombinator.com/item?id=${selectedItem!!.id}"
                            )

                            FeedbackButton(navController, selectedItem!!.id)
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            when (pageState.value) {
                is NewsPageState.Loading -> {
                    LoadingScreen()

                    LaunchedEffect(Unit) {
                        itemCollection.loadAll()
                    }
                }
                is NewsPageState.NewsIdsError -> {
                    ErrorScreen()
                }
                is NewsPageState.NewsIdsLoaded -> {

                    when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Compact,
                        WindowWidthSizeClass.Medium -> {
                            ItemInfiniteList(
                                onClick = onClick,
                                onClickComments = onClickComments,
                                modifier = Modifier.padding(top = it.calculateTopPadding())
                            )
                        }
                        WindowWidthSizeClass.Expanded -> {
                            Row(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                ItemInfiniteList(
                                    onClick = onClick,
                                    onClickComments = onClickComments,
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .padding(top = it.calculateTopPadding())
                                )

                                when (selectedItem) {
                                    null -> Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text("No article selected")
                                    }
                                    else -> {
                                        ArticlePage(
                                            navController = navController,
                                            windowWidthSizeClass = WindowWidthSizeClass.Compact,
                                            id = selectedItem!!.id,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ItemInfiniteList(
    modifier: Modifier = Modifier,
    onClick: (Item) -> Unit,
    onClickComments: (Item) -> Unit,
) {

    val lazyListState = rememberLazyListState()
    val viewModel: HomePageViewModel = viewModel()

    val itemCollection = viewModel.collections[TopStories]!!
    val itemListState = itemCollection.itemListFlow.collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()

    val refreshState = rememberSwipeRefreshState(isRefreshing = false)

    SwipeRefresh(
        state = refreshState,
        refreshTriggerDistance = 120.dp,
        onRefresh = {
            coroutineScope.launch {
                Timber.d("refreshing")

                itemCollection.refreshAll()
            }
        },
    ) {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
        ) {
            itemsIndexed(itemListState.value, key = { _, item -> item.itemId }) { index, itemState ->
                when (itemState) {
                    is NewsItemState.Loading, is NewsItemState.ItemError -> {
                        LaunchedEffect(index) {
                            itemCollection.requestItem(itemState.itemId)
                        }

                        NewsItem(placeholder = true)
                    }
                    is NewsItemState.ItemLoaded -> {
                        SwipeableNewsItem(
                            itemState.item,
                            onClick = { onClick(itemState.item) },
                            onClickComments = { onClickComments(itemState.item) },
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

@Composable
fun ErrorScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Error loading news. Please be sure you are online.")
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

sealed class NewsPageRoutes

data class HackerNewsView(val query: ItemCollection) : NewsPageRoutes() {
    val route: String = query::class.java.simpleName
}
