package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.components.NewsItem
import it.devddk.hackernewsclient.domain.model.utils.*
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.NewsPageState
import kotlinx.coroutines.launch


@Composable
@ExperimentalMaterial3Api
fun NewsPage(navController: NavController, route: NewsPageRoutes) {
    val viewModel: HomePageViewModel = viewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    val pageState = viewModel.pageState.collectAsState(NewsPageState.Loading)

    val query = viewModel.query.collectAsState(initial = TopStories)

    LaunchedEffect(route) {
        when (route) {
            is HackerNewsView -> viewModel.setQuery(route.query)
        }
    }

    systemUiController.setStatusBarColor(
        color = Color.Red,
        darkIcons = useDarkIcons
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            Text(
                stringResource(R.string.app_name),
                modifier = Modifier.padding(28.dp)
            )
            ALL_QUERIES.forEach {
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = ROUTE_ICONS[HackerNewsView(it).query]!!,
                            contentDescription = HackerNewsView(it).query.entryName
                        )
                    },
                    label = { Text(HackerNewsView(it).query.entryName) },
                    selected = it == (route as HackerNewsView).query,
                    onClick = { navController.navigate(HackerNewsView(it).route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row {
                            Icon(
                                ROUTE_ICONS[query.value]!!,
                                query.value.entryName,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp)
                            )

                            Text(query.value.entryName)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(Icons.Rounded.Menu, "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("search")
                        }) {
                            Icon(Icons.Rounded.Search, "Search")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Rounded.AccountCircle, "Notifications")
                        }
                    },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) {

            when (val currPageState = pageState.value) {
                is NewsPageState.Loading -> LoadingScreen()
                is NewsPageState.NewsIdsError -> ErrorScreen()
                is NewsPageState.NewsIdsLoaded -> ItemInfiniteList(navController,
                    modifier = Modifier.padding(top = it.calculateTopPadding()))
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


    LazyColumn(
        modifier = modifier,
        state = lazyListState
    ) {
        itemsIndexed(itemListState.value) { index, itemState ->

            when (itemState) {
                is NewsItemState.ItemLoaded -> {
                    NewsItem(
                        itemState.item,
                        onClick = { navController.navigate("items/${itemState.item.id}") },
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

val ROUTE_ICONS = mapOf(
    TopStories to Icons.Filled.TrendingUp,
    NewStories to Icons.Filled.NewReleases,
    BestStories to Icons.Filled.AutoAwesome,
    AskStories to Icons.Filled.QuestionAnswer,
    ShowStories to Icons.Filled.Dashboard,
    JobStories to Icons.Filled.Work
)