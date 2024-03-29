package it.devddk.hackernewsclient.shared.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import com.google.accompanist.pager.ExperimentalPagerApi
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.shared.components.comments.CommentItem
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class CommentItemUITest {
    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
        ExperimentalMaterial3WindowSizeClassApi::class)
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onClickAction_works() {
        var clickCount = 0
        val randomItem = Item(1, ItemType.COMMENT, by = "user", time = LocalDateTime.now())
        composeTestRule.setContent {

            MaterialTheme {
                CommentItem(item = randomItem, onClick = {
                    clickCount += 1
                })
            }
        }

        composeTestRule.onNode(hasClickAction()).performClick()
        assertEquals(1, clickCount)
    }
}
