package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import it.devddk.hackernewsclient.pages.ArticlePage
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
import it.devddk.hackernewsclient.pages.AboutPage
import it.devddk.hackernewsclient.pages.FeedbackPage
import it.devddk.hackernewsclient.pages.HackerNewsView
import it.devddk.hackernewsclient.pages.NewsPage
import it.devddk.hackernewsclient.pages.SearchPage
import it.devddk.hackernewsclient.pages.SettingsPage
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme

@ExperimentalPagerApi
@ExperimentalMaterial3Api
@ExperimentalMaterial3WindowSizeClassApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            HackerNewsClientTheme {
                val systemUiController = rememberSystemUiController()

                systemUiController.setStatusBarColor(
                    color = MaterialTheme.colorScheme.background,
                    darkIcons = !isSystemInDarkTheme()
                )

                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRootNavigator(windowSizeClass)
                }
            }
        }
    }

    @Composable
    private fun AppRootNavigator(windowSizeClass: WindowSizeClass) {
        val navController = rememberNavController()

        NavHost(navController = navController, "TopStories") {
            ALL_QUERIES.forEach { query ->
                composable(route = HackerNewsView(query).route) {
                    NewsPage(
                        navController = navController,
                        windowSizeClass = windowSizeClass,
                        route = HackerNewsView(query)
                    )
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
                ArticlePage(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
                    id = backStackEntry.arguments?.getInt("itemId")!!
                )
            }

            composable("search") {
                SearchPage(navController = navController)
            }

            composable("settings") {
                SettingsPage(navController = navController)
            }

            composable("about") {
                AboutPage(navController = navController)
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
                ArticlePage(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
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
                ArticlePage(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
                    id = backStackEntry.arguments?.getInt("itemId")!!,
                    selectedView = "comments"
                )
            }
        }
    }
}
