package it.devddk.hackernewsclient.shared.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.navigation.NavController
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import it.devddk.hackernewsclient.domain.model.collection.ALL_QUERIES
import it.devddk.hackernewsclient.pages.news.HackerNewsView
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HNModalNavigatorUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>()

    @Before
    fun mockTheNavController() {
        mockNavController.apply {
            every { navigate("homepage") } just runs
            ALL_QUERIES.forEach { collection ->
                every { navigate(HackerNewsView(collection).route) } just runs
            }
            every { navigate(any<String>()) } just runs
            every { currentDestination?.route } returns "homepage"
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun hnModalNavigator_navigationIsCalled() {
        composeTestRule.apply {
            setContent {
                val state = rememberDrawerState(initialValue = DrawerValue.Open)
                HNModalNavigatorPanel(navController = mockNavController, state = state) {

                }

            }
            ALL_QUERIES.forEach {
                onNodeWithContentDescription(it.entryName).assertExists().assertHasClickAction()
                    .performClick()
            }
            onNodeWithContentDescription("Settings").assertExists().assertHasClickAction()
                .performClick()
            onNodeWithContentDescription("About").assertExists().assertHasClickAction()
                .performClick()
            onNodeWithContentDescription("homepage").assertExists().assertHasClickAction()
                .performClick()
            // Lands in home
            ALL_QUERIES.forEach {
                verify { mockNavController.navigate(HackerNewsView(it).route) }

            }
            verify { mockNavController.navigate("settings")}
            verify { mockNavController.navigate("about")}
            verify { mockNavController.navigate("homepage")}
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun hnModalNavigator_subContentIsDisplayed_whenOpen() {
        composeTestRule.apply {
            setContent {
                val state = rememberDrawerState(initialValue = DrawerValue.Open)
                HNModalNavigatorPanel(navController = mockNavController, state = state) {
                    Text(text = "Content")
                }
            }
            // Content still exstits under
            onNodeWithText("Content").assertExists()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun hnModalNavigator_subContentIsDisplayed_whenClosed() {
        composeTestRule.apply {
            setContent {
                val state = rememberDrawerState(initialValue = DrawerValue.Closed)
                HNModalNavigatorPanel(navController = mockNavController, state = state) {
                    Text(text = "Content")
                }
            }
            onNodeWithText("Content").assertExists()
        }
    }


}