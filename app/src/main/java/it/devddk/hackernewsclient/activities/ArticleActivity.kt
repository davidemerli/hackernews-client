package it.devddk.hackernewsclient.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.glance.action.ActionParameters
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import it.devddk.hackernewsclient.pages.ArticlePage
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme

val ItemIdKey = ActionParameters.Key<Int>("itemIdKey")

@ExperimentalPagerApi
@ExperimentalMaterial3Api
@ExperimentalMaterial3WindowSizeClassApi
class ArticleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemId = intent.getIntExtra(ItemIdKey.name, -1)

        if (itemId == -1) {
            throw IllegalArgumentException("ItemId is required")
        }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            HackerNewsClientTheme {
                val systemUiController = rememberSystemUiController()

                systemUiController.setStatusBarColor(
                    color = MaterialTheme.colorScheme.background,
                    darkIcons = !isSystemInDarkTheme()
                )

                Surface(color = MaterialTheme.colorScheme.background) {
                    ArticlePage(
                        navController = rememberNavController(),
                        windowWidthSizeClass = windowSizeClass.widthSizeClass,
                        id = itemId
                    )
                }
            }
        }
    }
}
