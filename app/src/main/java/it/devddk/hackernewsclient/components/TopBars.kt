package it.devddk.hackernewsclient.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.devddk.hackernewsclient.R
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
@OptIn(ExperimentalMaterial3Api::class)
fun HNModalNavigatorPanel(
    navController: NavController,
    state: DrawerState,
    query: String,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = state,
        gesturesEnabled = state.isOpen,
        drawerContent = {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.padding(28.dp)
            )
            ALL_QUERIES.forEach {
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = ROUTE_ICONS[HackerNewsView(it).route]!!,
                            contentDescription = query
                        )
                    },
                    label = { Text(HackerNewsView(it).route) },
                    selected = HackerNewsView(it).route == query,
                    onClick = { navController.navigate(HackerNewsView(it).route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = { Text("Settings") },
                selected = query == "Settings",
                onClick = { navController.navigate("settings") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    ) {
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
