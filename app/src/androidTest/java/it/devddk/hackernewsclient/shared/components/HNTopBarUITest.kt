package it.devddk.hackernewsclient.shared.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import androidx.webkit.WebViewFeature
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import it.devddk.hackernewsclient.MainActivity
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.pages.home.components.HNTopBar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class HNTopBarUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>()

    @Before
    fun mockTheNavController() {
        mockNavController.apply {
            every { navigate("search/test") } just runs
            every { navigate(any<String>()) } just runs
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun topBarSearchToggle_toggles() {
        composeTestRule.apply {
            setContent {
                HNTopBar(navController = mockNavController)
            }
            onNodeWithText("Hacker News", substring = true).assertExists()
            onNodeWithContentDescription("Search")
                .assertExists().performClick()
            onNodeWithText("Hacker News", substring = true).assertDoesNotExist()
            onNodeWithContentDescription("Close Search").assertExists().performClick()
            onNodeWithText("Hacker News", substring = true).assertExists()
            onNodeWithContentDescription("Search")
                .assertExists()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
    @Test
    fun navigation_isCalled() {
        composeTestRule.apply {
            setContent {
                HNTopBar(navController = mockNavController)
            }

            onNodeWithContentDescription("Search")
                .assertExists().performClick()

            onNode(hasImeAction(ImeAction.Search)).assertExists().performTextInput("test")
            onNode(hasImeAction(ImeAction.Search)).performImeAction()
        }

        mockNavController.apply {
            verify { navigate("search/test")  }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
    @Test
    fun itemTopBar_itemAreShownInArticle() {
        composeTestRule.apply {
            setContent {
                HNTopBar(navController = mockNavController, isOnArticle = true, selectedItem = Item(10, type = ItemType.STORY, by = "giovanni", time = LocalDateTime.now()))
            }

            onNodeWithContentDescription("Search")
                .assertDoesNotExist()

            onNodeWithContentDescription("Share").assertExists().assertHasClickAction()
            onNodeWithContentDescription("Open in browser").assertExists().assertHasClickAction()
            onNodeWithContentDescription("Reader Mode").assertExists().assertHasClickAction()
            if(WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                onNodeWithContentDescription("Dark Mode").assertExists().assertHasClickAction()
            } else {
                onNodeWithContentDescription("Dark Mode").assertDoesNotExist()
            }
            onNodeWithContentDescription("More").assertExists().assertHasClickAction()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
    @Test
    fun itemTopBar_itemAreShownNotInArticle() {
        composeTestRule.apply {
            setContent {
                HNTopBar(navController = mockNavController, isOnArticle = false, selectedItem = Item(10, type = ItemType.STORY, by = "giovanni", time = LocalDateTime.now(), url = "www.com"))
            }

            onNodeWithContentDescription("Search")
                .assertDoesNotExist()

            onNodeWithContentDescription("Share").assertExists().assertHasClickAction()
            onNodeWithContentDescription("Open in browser").assertExists().assertHasClickAction()
            onNodeWithContentDescription("Reader Mode").assertDoesNotExist()
            onNodeWithContentDescription("Dark Mode").assertDoesNotExist()
            onNodeWithContentDescription("More").assertExists().assertHasClickAction()
        }
    }
}