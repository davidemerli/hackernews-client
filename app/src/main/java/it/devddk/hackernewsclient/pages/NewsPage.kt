package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import it.devddk.hackernewsclient.components.NewsItem
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.StoryItem
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.components.ErrorItem
import it.devddk.hackernewsclient.components.LoadingItem
import it.devddk.hackernewsclient.domain.model.utils.*
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.ItemState


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NewsPage(navController: NavController, route: NewsPageRoutes) {
    val scrollState = rememberLazyListState()
    val viewModel: HomePageViewModel = viewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val itemListState = viewModel.shownList.collectAsState(initial = emptyList())

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
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.app_name)) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                onClick = {},
                selected = false
            )
            ALL_QUERIES.forEach {
                NavigationDrawerItem(
                    label = { Text(HackerNewsView(it).route) },
                    selected = it == (route as HackerNewsView).query,
                    onClick = { navController.navigate(HackerNewsView(it).route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(2.dp))
            }
        }) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
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
                        IconButton(onClick = { }) {
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
            LazyColumn(
                Modifier.padding(top = 64.dp),
                state = scrollState
            ) {

                scope.launch {
                    viewModel.requestMore(scrollState.firstVisibleItemIndex + 30)
                }

                itemsIndexed(itemListState.value) { _, msgState ->
                    when (msgState) {
                        is ItemState.ItemLoaded -> NewsItem(msgState.item)
                        is ItemState.Loading -> LoadingItem()
                        is ItemState.ItemError -> ErrorItem()
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 441, heightDp = 980)
@Composable
fun DefaultPreview2() {
    Test()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // icons to mimic drawer destinations
    val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)
    val selectedItem = remember { mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            items.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item, contentDescription = null) },
                    label = { Text(item.name) },
                    selected = item == selectedItem.value,
                    onClick = {
                        scope.launch { drawerState.close() }
                        selectedItem.value = item
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = if (drawerState.isClosed) ">>> Swipe >>>" else "<<< Swipe <<<")
                Spacer(Modifier.height(20.dp))
                Button(onClick = { scope.launch { drawerState.open() } }) {
                    Text("Click to open")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 441, heightDp = 980)
@Composable
fun DefaultPreview(name: String = "peppe") {
    val scrollState = rememberScrollState()

    HackerNewsClientTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.app_name)) },
                        navigationIcon = { Icon(Icons.Rounded.Menu, "Menu") },
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(Icons.Rounded.Search, "Search")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Rounded.AccountCircle, "Notifications")
                            }
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(4.dp),
                ) {
                    NewsItem(Item(
                        StoryItem(14,
                            false,
                            Expandable.compressed("giovanni"),
                            LocalDateTime.now(),
                            false,
                            emptyMap(),
                            "Super articol",
                            15,
                            12,
                            "www.com",
                            null)
                    ))
                    Spacer(modifier = Modifier.height(2.dp))
                    NewsItem(Item(
                        StoryItem(14,
                            false,
                            Expandable.compressed("giovanni"),
                            LocalDateTime.now(),
                            false,
                            emptyMap(),
                            "Super articol",
                            15,
                            12,
                            "www.com",
                            null)
                    ))
                }
            }
        }
    }
}

sealed class NewsPageRoutes

data class HackerNewsView(val query: CollectionQueryType) : NewsPageRoutes() {
    val route = query::class.java.simpleName
}