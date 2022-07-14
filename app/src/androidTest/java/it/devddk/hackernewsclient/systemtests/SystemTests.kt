package it.devddk.hackernewsclient.systemtests

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import com.google.accompanist.pager.ExperimentalPagerApi
import it.devddk.hackernewsclient.MainActivity
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
import org.junit.Rule
import org.junit.Test

class SystemTests {
    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
        ExperimentalMaterial3WindowSizeClassApi::class)
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
        ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun menu_navigationWorks() {
        composeTestRule.apply {

            ALL_QUERIES.forEach {
                onNodeWithContentDescription("Menu")
                    .assertExists().performClick()
                onAllNodesWithContentDescription(it.entryName).filterToOne(hasClickAction()).performClick()
                onNodeWithContentDescription(it.entryName).assertExists()
            }
            onNodeWithContentDescription("Menu")
                .assertExists().performClick()
            onAllNodesWithContentDescription("Favorites").filterToOne(hasClickAction()).performClick()
            onNodeWithContentDescription("Favorites").assertExists()
        }
    }

    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
        ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun search_works() {
        composeTestRule.apply {
            onNodeWithContentDescription("Search").assertExists().performClick()
            onNodeWithContentDescription("Search").assertDoesNotExist()
            onNode(hasImeAction(ImeAction.Search)).assertExists().performTextInput("test")
            onNode(hasImeAction(ImeAction.Search)).performImeAction()
            onAllNodesWithText("test", substring = true, ignoreCase = true).assertAny(hasText("test", substring = true, ignoreCase = true))
        }
    }

}