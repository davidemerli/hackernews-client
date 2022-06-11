package it.devddk.hackernewsclient.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.utils.ALL_QUERIES
import it.devddk.hackernewsclient.pages.HackerNewsView
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomePageTopBar(
    navController: NavController,
    state: DrawerState,
    query: String,
) {
    val scope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            Row {
                Icon(
                    ROUTE_ICONS[query]!!,
                    query,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp)
                )

                Text(query)
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch { state.open() }
            }) {
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
}

@Composable
fun SingleNewsPageTopBar(item: Item, navController: NavController) {
    val context = LocalContext.current
    var openInBrowserExpanded by remember { mutableStateOf(false) }

    SmallTopAppBar(
        modifier = Modifier.wrapContentHeight(Alignment.Bottom),
        title = { },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBack, "Back")
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart)
                    .offset(x = 10.dp, y = (-10).dp)
            ) {
                item.url?.let { url ->
                    IconButton(onClick = { openInBrowser(context = context, url = url) }) {
                        Icon(Icons.Filled.OpenInBrowser, "Open in browser")
                    }
                }

                DropdownMenu(
                    expanded = openInBrowserExpanded,
                    onDismissRequest = { openInBrowserExpanded = false }
                ) {
                    item.url?.let { url ->
                        DropdownMenuItem(
                            text = { Text("Share Article") },
                            leadingIcon = {
                                Icon(Icons.Filled.Share, contentDescription = "Share Article")
                            },
                            onClick = {
                                openInBrowser(context, url)
                                openInBrowserExpanded = false
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Share HN link") },
                        leadingIcon = {
                            Icon(Icons.Filled.Share, contentDescription = "Share HN link")
                        },
                        onClick = {
                            openInBrowser(
                                context,
                                "https://news.ycombinator.com/item?id=${item.id}"
                            )
                            openInBrowserExpanded = false
                        },
                    )
                }
            }
        },
    )
}

fun openInBrowser(context: Context, url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW)
    browserIntent.data = Uri.parse(url)
    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    ContextCompat.startActivity(context, browserIntent, null)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HNModalNavigatorPanel(
    navController: NavController,
    state: DrawerState,
    query: String,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(drawerState = state, gesturesEnabled = state.isOpen, drawerContent = {
        Text(text = stringResource(R.string.app_name), modifier = Modifier.padding(28.dp))

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
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            },
            label = { Text("Settings") },
            selected = query == "Settings",
            onClick = { navController.navigate("settings") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }) {
        content()
    }
}

val ROUTE_ICONS = mapOf(
    "TopStories" to Icons.Filled.TrendingUp,
    "NewStories" to Icons.Filled.NewReleases,
    "BestStories" to Icons.Filled.AutoAwesome,
    "AskStories" to Icons.Filled.QuestionAnswer,
    "ShowStories" to Icons.Filled.Dashboard,
    "JobStories" to Icons.Filled.Work,
    "Settings" to Icons.Filled.Settings
)

val ROUTE_TITLES = mapOf(
    "TopStories" to "Top Stories",
    "NewStories" to "New Stories",
    "BestStories" to "Best Stories",
    "AskStories" to "Ask HN",
    "ShowStories" to "Show HN",
    "JobStories" to "HN Jobs",
)
