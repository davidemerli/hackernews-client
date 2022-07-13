package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.devddk.hackernewsclient.domain.model.collection.ItemCollectionEntry
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class NewsItemUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testItem1 = Item(
        id = 1,
        type = ItemType.STORY,
        deleted = false, "giovanni",
        time = LocalDateTime.now(),
        kids = listOf(2, 3, 4),
        title = "title",
        descendants = 42,
        score = 55,
        url = "www.domain.com/sub",
        collections = mapOf(UserDefinedItemCollection.ReadLater to ItemCollectionEntry(
            UserDefinedItemCollection.ReadLater,
            LocalDateTime.now()),
            UserDefinedItemCollection.Favorites to ItemCollectionEntry(UserDefinedItemCollection.Favorites,
                LocalDateTime.now()))
    )

    private val jobItem = Item(
        id = 1,
        type = ItemType.JOB,
        deleted = false, null,
        time = LocalDateTime.now(),
        kids = listOf(2, 3, 4),
        title = "title",
        descendants = null,
        score = 55,
        url = "www.domain.com/sub",
        collections = mapOf(UserDefinedItemCollection.ReadLater to ItemCollectionEntry(
            UserDefinedItemCollection.ReadLater,
            LocalDateTime.now()),
            UserDefinedItemCollection.Favorites to ItemCollectionEntry(UserDefinedItemCollection.Favorites,
                LocalDateTime.now()))
    )

    @Test
    fun newsItem_displaysEverything() {
        var onClickCounter: Int = 0
        var onCommentClickedCounter: Int = 0

        composeTestRule.apply {

            setContent {
                val fav = mutableStateOf(true)
                val read = mutableStateOf(true)
                NewsItem(item = testItem1, favorite = fav, readLater = read,
                    onClick = {
                        onClickCounter += 1
                    },
                    onClickComments = {
                        onCommentClickedCounter += 1
                    })
            }

            onNodeWithText("title", substring = true, ignoreCase = true).assertExists()
            onNodeWithText("domain.com", substring = true, ignoreCase = true).assertExists()
            // Points
            onNodeWithText("55", substring = true, ignoreCase = true).assertExists()
            // Descendants
            onNodeWithText("42", substring = true, ignoreCase = true).assertExists()
            //Time
            onNodeWithText("now", substring = true, ignoreCase = true).assertExists().assertHasClickAction().performClick()
            assertEquals(0, onCommentClickedCounter)
            assertEquals(1, onClickCounter)
            //Time
            onNodeWithText("giovanni", substring = true, ignoreCase = true).assertExists()
                .assertHasClickAction()
            onNodeWithContentDescription("Comments").assertExists().assertHasClickAction()
                .performClick()
            assertEquals(1, onCommentClickedCounter)
            assertEquals(1, onClickCounter)
            onNodeWithContentDescription("Options").assertExists().assertHasClickAction()
        }
    }

    @Test
    fun newsItem_favoriteIconBehaviors() {
        composeTestRule.apply {
            val fav = mutableStateOf(true)
            val read = mutableStateOf(true)
            setContent {

                NewsItem(item = testItem1, favorite = fav, readLater = read)
            }
            onNodeWithContentDescription("Favorite").assertExists()
            onNodeWithContentDescription("Read later").assertExists()
            fav.value = false
            onNodeWithContentDescription("Favorite").assertDoesNotExist()
            onNodeWithContentDescription("Read later").assertExists()
            read.value = false
            onNodeWithContentDescription("Favorite").assertDoesNotExist()
            onNodeWithContentDescription("Read later").assertDoesNotExist()
            read.value = true
            onNodeWithContentDescription("Favorite").assertDoesNotExist()
            onNodeWithContentDescription("Read later").assertExists()
        }
    }

    @Test
    fun jobItem_noComments() {
        composeTestRule.apply {
            val fav = mutableStateOf(true)
            val read = mutableStateOf(true)
            setContent {
                NewsItem(item = jobItem, favorite = fav, readLater = read)
            }
            onNodeWithContentDescription("Comments").assertDoesNotExist()
        }
    }
}