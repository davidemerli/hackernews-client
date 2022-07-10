package it.devddk.hackernewsclient.shared.components

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class CommentCardUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController = mockk<NavController>()

    @Before
    fun mockTheNavController() {
        mockNavController.apply {
            every { navigate(any<String>()) } just runs
        }
    }

    @Test
    fun author_displayOP() {

        val rootItem = Item(1, ItemType.STORY, by = "user", time = LocalDateTime.now())
        val item = Item(4, ItemType.COMMENT, by = "user", time = LocalDateTime.now())
        val otherItem = Item(4, ItemType.COMMENT, by = "anotherUser", time = LocalDateTime.now())
        composeTestRule.setContent {

            MaterialTheme {
                val lazy = rememberLazyListState()
                CommentCard(item = item,
                    fontSize = 16.sp,
                    rootItem = rootItem,
                    expanded = false,
                    listState = lazy,
                    navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("${item.by} (OP)").assertExists()
        composeTestRule.onNodeWithText("${item.by}").assertDoesNotExist()
    }


}