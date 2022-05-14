package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
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
import it.devddk.hackernewsclient.components.ErrorItem
import it.devddk.hackernewsclient.components.LoadingItem
import it.devddk.hackernewsclient.components.NewsItem
import it.devddk.hackernewsclient.domain.model.utils.ALL_QUERIES
import it.devddk.hackernewsclient.domain.model.utils.CollectionQueryType
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.ItemState
import kotlinx.coroutines.launch


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

sealed class NewsPageRoutes

data class HackerNewsView(val query: CollectionQueryType) : NewsPageRoutes() {
    val route = query::class.java.simpleName
}