package it.devddk.hackernewsclient.pages.home

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
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
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
import it.devddk.hackernewsclient.pages.news.HackerNewsView
import it.devddk.hackernewsclient.shared.components.topbars.ROUTE_ICONS
import it.devddk.hackernewsclient.shared.components.topbars.ROUTE_TITLES
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.ItemCollectionHolder
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomePage(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
) {
    val viewModel: HomePageViewModel = viewModel()

    val bestCollection = viewModel.collections[BestStories]!!
    val topCollection = viewModel.collections[TopStories]!!
    val readLaterCollection = viewModel.collections[UserDefinedItemCollection.ReadLater]!!

    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var expandedArticleView by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val onItemClick = { item: Item ->
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            selectedItem = item
        } else {
            navController.navigate("items/${item.id}")
        }
    }

    val onItemClickComments = { item: Item ->
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            selectedItem = item //TODO: go to comments in tabbed view
        } else {
            navController.navigate("items/${item.id}/comments")
        }
    }

    HNModalNavigatorPanel(navController = navController, state = drawerState) {
        Scaffold(
            topBar = {
                HNTopBar(
                    navController = navController,
                    drawerState = drawerState,
                    selectedItem = selectedItem,
                    onClose = { selectedItem = null },
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
                        onItemClick = onItemClick,
                        onItemClickComments = onItemClickComments,
                        selectedItem = selectedItem,
                        expanded = expandedArticleView,
                        onExpandedClick = { expandedArticleView = !expandedArticleView }
                    )
                }
                WindowWidthSizeClass.Compact,
                WindowWidthSizeClass.Medium -> {
                    CompactLayout(
                        modifier = Modifier.padding(top = it.calculateTopPadding()),
                        navController = navController,
                        bestCollection = bestCollection,
                        topCollection = topCollection,
                        readLaterCollection = readLaterCollection,
                        onItemClick = onItemClick,
                        onItemClickComments = onItemClickComments,
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
) {
    val webViewState = rememberWebViewState(url = selectedItem?.url ?: "")
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
                onItemClick = onItemClick,
                onItemClickComments = onItemClickComments,
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
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val readLaterItems = readLaterCollection.itemListFlow.collectAsState(initial = emptyList())
    val showReadLater by derivedStateOf { readLaterItems.value.isNotEmpty() }

    val viewModel: HomePageViewModel = viewModel()

    LaunchedEffect(readLaterCollection) {
        readLaterCollection.loadAll()
    }

    SwipeRefresh(
        state = swipeRefreshState,
        refreshTriggerDistance = 144.dp,
        onRefresh = {
            coroutineScope.launch { bestCollection.loadAll() }

            coroutineScope.launch { topCollection.loadAll() }

            coroutineScope.launch { readLaterCollection.loadAll() }
        }
    ) {
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
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 256.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 384.dp),
                itemCollection = topCollection,
                onItemClick = onItemClick,
                addToCollection = { item, itemCollection ->
                    coroutineScope.launch {
                        viewModel.collections[itemCollection]?.addToFavorites(item.id, itemCollection)
                    }
                }
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HNModalNavigatorPanel(
    navController: NavController,
    state: DrawerState,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()
    val query = navController.currentDestination?.route

    ModalNavigationDrawer(
        drawerState = state,
        gesturesEnabled = state.isOpen,
        drawerContent = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                Text(text = stringResource(R.string.app_name), modifier = Modifier.padding(28.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = query) },
                    label = { Text("Homepage") },
                    selected = query == "homepage",
                    onClick = { navController.navigate("homepage") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                ALL_QUERIES.forEach {
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = ROUTE_ICONS[HackerNewsView(it).route]!!,
                                contentDescription = query
                            )
                        },
                        label = { Text(ROUTE_TITLES[HackerNewsView(it).route]!!) },
                        selected = HackerNewsView(it).route == query,
                        onClick = { navController.navigate(HackerNewsView(it).route) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Divider(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    },
                    label = { Text("Settings") },
                    selected = query == "settings",
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Filled.Feedback, contentDescription = "Feedback")
                    },
                    label = { Text("Feedback") },
                    selected = query == "feedback",
                    onClick = { navController.navigate("feedback") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    },
                    label = { Text("About/Contacts") },
                    selected = query == "about",
                    onClick = { navController.navigate("about") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        content()
    }
}
