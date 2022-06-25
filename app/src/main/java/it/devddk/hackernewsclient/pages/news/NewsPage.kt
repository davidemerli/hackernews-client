package it.devddk.hackernewsclient.pages.news

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import it.devddk.hackernewsclient.domain.model.collection.ItemCollection
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.shared.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.shared.components.HomePageTopBar
import it.devddk.hackernewsclient.shared.components.news.NewsItem
import it.devddk.hackernewsclient.shared.components.news.SwipeableNewsItem
import it.devddk.hackernewsclient.viewmodels.NewsListViewModel
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.NewsPageState
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NewsPage(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    route: NewsPageRoutes,
) {
    val viewModel: NewsListViewModel = viewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val pageState = viewModel.pageState.collectAsState(NewsPageState.Loading)

    val query = viewModel.currentQuery.collectAsState(initial = TopStories)

    LaunchedEffect(route) {
        when (route) {
            is HackerNewsView -> viewModel.setQuery(route.query)
        }
    }

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState,
        query = HackerNewsView(query.value).route,
    ) {
        Scaffold(
            topBar = {
                HomePageTopBar(
                    navController = navController,
                    state = drawerState,
                    query = HackerNewsView(query.value).route
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            when (pageState.value) {
                is NewsPageState.Loading -> LoadingScreen()
                is NewsPageState.NewsIdsError -> ErrorScreen()
                is NewsPageState.NewsIdsLoaded -> ItemInfiniteList(
                    navController,
                    modifier = Modifier.padding(top = it.calculateTopPadding())
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ItemInfiniteList(navController: NavController, modifier: Modifier = Modifier) {

    val lazyListState = rememberLazyListState()
    val viewModel: NewsListViewModel = viewModel()
    val itemListState =
        viewModel.itemListFlow.collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()

    val refreshState = rememberSwipeRefreshState(isRefreshing = false)

    SwipeRefresh(
        state = refreshState,
        onRefresh = {
            coroutineScope.launch {
                Timber.d("refreshing")
                viewModel.refreshPage()
            }
        },
    ) {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
        ) {
            itemsIndexed(itemListState.value,  key = { _, item -> item.itemId }) { index, itemState ->
                when (itemState) {
                    is NewsItemState.ItemLoaded -> {
                        key(itemState.item.id) {
                            SwipeableNewsItem(
                                itemState.item,
                                onClick = {
                                    navController.navigate(
                                        "items/${itemState.item.id}"
                                    )
                                },
                                onClickComments = {
                                    navController.navigate(
                                        "items/${itemState.item.id}/comments"
                                    )
                                },
                                placeholder = false
                            )
                        }
                    }
                    is NewsItemState.Loading, is NewsItemState.ItemError -> {
                        LaunchedEffect(index) {
                            viewModel.requestItem(index)
                        }

                        NewsItem(placeholder = true)
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
    Text("Error")
}

@Composable
fun LoadingScreen() {
    Text("Loading")
}

sealed class NewsPageRoutes

data class HackerNewsView(val query: ItemCollection) : NewsPageRoutes() {
    val route: String = query::class.java.simpleName
}
