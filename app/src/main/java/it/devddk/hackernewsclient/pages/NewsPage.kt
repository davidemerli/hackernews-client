package it.devddk.hackernewsclient.pages

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.components.HomePageTopBar
import it.devddk.hackernewsclient.components.NewsItem
import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.domain.model.utils.TopStories
import it.devddk.hackernewsclient.utils.encodeJson
import it.devddk.hackernewsclient.utils.urlEncode
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.NewsPageState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NewsPage(navController: NavController, route: NewsPageRoutes) {
    val viewModel: HomePageViewModel = viewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val pageState = viewModel.pageState.collectAsState(NewsPageState.Loading)

    val query = viewModel.uiState.collectAsState(initial = TopStories)

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
    val viewModel: HomePageViewModel = viewModel()
    val itemListState =
        viewModel.itemListFlow.collectAsState(initial = List(TopStories.maxAmount) { NewsItemState.Loading })

    val singleNewsViewModel: SingleNewsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        state = lazyListState
    ) {
        itemsIndexed(itemListState.value) { index, itemState ->
            when (itemState) {
                is NewsItemState.ItemLoaded -> {
                    NewsItem(
                        itemState.item,
                        onClick = {
                            navController.navigate("items/preloaded/${itemState.item.encodeJson().urlEncode()}")
                        },
                        placeholder = false
                    )
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

            itemListState.value[index]
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

data class HackerNewsView(val query: CollectionQueryType) : NewsPageRoutes() {
    val route: String = query::class.java.simpleName
}
