package it.devddk.hackernewsclient.shared.components.topbars

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.items.Item
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomePageTopBar(
    navController: NavController,
    state: DrawerState,
    query: String,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
    val scope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            QueryTitle(query)
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch { state.open() }
            }) {
                Icon(Icons.Rounded.Menu, "Menu")
            }
        },
        actions = actions,
    )
}

@Composable
fun SingleNewsPageTopBar(modifier: Modifier = Modifier, item: Item, navController: NavController) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    SmallTopAppBar(
        modifier = modifier.wrapContentHeight(Alignment.Bottom),
        title = { },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = {
                // TODO
            }) {
                Icon(Icons.Filled.Refresh, "Refresh")
            }

            FeedbackButton(navController, item.id)

            OpenInBrowserButton(
                itemUrl = item.url,
                hnUrl = "https://news.ycombinator.com/item?id=${item.id}"
            )
        },
    )
}

fun openInBrowser(context: Context, url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW)
    browserIntent.data = Uri.parse(url)
    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    ContextCompat.startActivity(context, browserIntent, null)
}

val ROUTE_ICONS = mapOf(
    "TopStories" to Icons.Filled.TrendingUp,
    "NewStories" to Icons.Filled.NewReleases,
    "BestStories" to Icons.Filled.AutoAwesome,
    "AskStories" to Icons.Filled.QuestionAnswer,
    "ShowStories" to Icons.Filled.Dashboard,
    "JobStories" to Icons.Filled.Work,
    "Settings" to Icons.Filled.Settings,
    "Feedback" to Icons.Filled.Feedback,
    "Favorites" to Icons.Filled.Favorite,
    "ReadLater" to Icons.Filled.Update,
    "About" to Icons.Filled.Info
)

val ROUTE_TITLES = mapOf(
    "TopStories" to "Top Stories",
    "NewStories" to "New Stories",
    "BestStories" to "Best Stories",
    "AskStories" to "Ask HN",
    "ShowStories" to "Show HN",
    "JobStories" to "HN Jobs",
    "Favorites" to "Favorites",
    "ReadLater" to "Read Later",
    "About" to "About"
)
