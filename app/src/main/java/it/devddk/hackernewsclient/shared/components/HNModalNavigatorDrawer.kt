package it.devddk.hackernewsclient.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
import it.devddk.hackernewsclient.pages.news.HackerNewsView
import it.devddk.hackernewsclient.shared.components.topbars.ROUTE_ICONS
import it.devddk.hackernewsclient.shared.components.topbars.ROUTE_TITLES

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
                    icon = { Icon(Icons.Filled.Home, contentDescription = "homepage") },
                    label = { Text("Homepage") },
                    selected = query == "homepage",
                    onClick = { navController.navigate("homepage") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                ALL_QUERIES.forEachIndexed { index, itemCollection ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = ROUTE_ICONS[HackerNewsView(itemCollection).route]!!,
                                contentDescription = itemCollection.entryName
                            )
                        },
                        label = { Text(ROUTE_TITLES[HackerNewsView(itemCollection).route]!!) },
                        selected = HackerNewsView(itemCollection).route == query,
                        onClick = { navController.navigate(HackerNewsView(itemCollection).route) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    if (index == ALL_QUERIES.size - 3) {
                        Divider(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
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
