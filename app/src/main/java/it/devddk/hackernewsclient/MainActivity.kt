package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import it.devddk.hackernewsclient.domain.model.utils.ALL_QUERIES
import it.devddk.hackernewsclient.pages.HackerNewsView
import it.devddk.hackernewsclient.pages.NewsPage
import it.devddk.hackernewsclient.pages.SearchPage
import it.devddk.hackernewsclient.pages.SettingsPage
import it.devddk.hackernewsclient.pages.SingleNewsPage
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme

@ExperimentalPagerApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HackerNewsClientTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRootNavigator()
                }
            }
        }
    }

    @Composable
    private fun AppRootNavigator() {
        val navController = rememberNavController()

        NavHost(navController = navController, "TopStories") {
            ALL_QUERIES.forEach { query ->
                composable(route = HackerNewsView(query).route) {
                    NewsPage(navController, route = HackerNewsView(query))
                }
            }

            composable(
                "items/{itemId}",
                arguments = listOf(
                    navArgument("itemId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                SingleNewsPage(
                    navController = navController,
                    id = backStackEntry.arguments?.getInt("itemId")!!
                )
            }

            composable("search") {
                SearchPage(navController = navController)
            }

            composable("settings") {
                SettingsPage(navController = navController)
            }
        }
    }
}
