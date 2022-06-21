package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
import it.devddk.hackernewsclient.pages.FeedbackPage
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
                val systemUiController = rememberSystemUiController()

                systemUiController.setStatusBarColor(
                    color = MaterialTheme.colorScheme.background,
                    darkIcons = !isSystemInDarkTheme()
                )

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

            composable("feedback") {
                FeedbackPage(navController = navController)
            }

            composable(
                "feedback/{itemId}",
                arguments = listOf(
                    navArgument("itemId") {
                        type = NavType.IntType
                    }
                ),
            ) { backStackEntry ->
                FeedbackPage(
                    navController = navController,
                    itemId = backStackEntry.arguments?.getInt("itemId")!!
                )
            }

            composable(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://news.ycombinator.com/item?id={itemId}" }
                ),
                arguments = listOf(
                    navArgument("itemId") {
                        type = NavType.IntType
                    }
                ),
                route = "items/{itemId}"
            ) { backStackEntry ->
                SingleNewsPage(
                    navController = navController,
                    id = backStackEntry.arguments?.getInt("itemId")!!
                )
            }

            composable(
                arguments = listOf(
                    navArgument("itemId") {
                        type = NavType.IntType
                    }
                ),
                route = "items/{itemId}/comments"
            ) { backStackEntry ->
                SingleNewsPage(
                    navController = navController,
                    id = backStackEntry.arguments?.getInt("itemId")!! ,
                    selectedView = "comments"
                )
            }
        }
    }
}
