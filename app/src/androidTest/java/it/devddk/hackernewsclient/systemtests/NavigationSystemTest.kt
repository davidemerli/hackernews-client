package it.devddk.hackernewsclient.systemtests

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.accompanist.pager.ExperimentalPagerApi
import it.devddk.hackernewsclient.MainActivity
import org.junit.Rule
import org.junit.Test

class NavigationSystemTest {
    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
        ExperimentalMaterial3WindowSizeClassApi::class)
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
        ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun test_search_toggle_works() {
        composeTestRule.apply {
            onNodeWithContentDescription("Search")
                .assertExists().performClick()
            onNodeWithContentDescription("Close Search").assertExists().performClick()
            onNodeWithContentDescription("Search")
                .assertExists()
        }

    }

}