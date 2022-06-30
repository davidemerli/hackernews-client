package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.favorite
import it.devddk.hackernewsclient.domain.model.items.readLater
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun SwipeableNewsItem(
    item: Item = placeholderItem,
    placeholder: Boolean = false,
    onClick: () -> Unit = {},
    onClickComments: () -> Unit = {},
    addToCollection: (Item, UserDefinedItemCollection) -> Unit = { _, _ -> },
) {
    val readLater = remember { mutableStateOf(item.collections.readLater) }
    val favorite = remember { mutableStateOf(item.collections.favorite) }

    val readLaterAction = SwipeAction(
        icon = rememberVectorPainter(if (!readLater.value) Icons.Filled.Update else Icons.Filled.UpdateDisabled),
        background = MaterialTheme.colorScheme.secondary,
        onSwipe = { addToCollection(item, UserDefinedItemCollection.ReadLater) }
    )

    val favoriteAction = SwipeAction(
        icon = rememberVectorPainter(if (!favorite.value) Icons.Filled.Star else Icons.Filled.StarOutline),
        background = MaterialTheme.colorScheme.tertiary,
        onSwipe = { addToCollection(item, UserDefinedItemCollection.Favorites) },
    )

    SwipeableActionsBox(
        startActions = listOf(favoriteAction),
        endActions = listOf(readLaterAction),
        swipeThreshold = 128.dp,
        backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.background
    ) {
        NewsItem(
            item = item,
            placeholder = placeholder,
            onClick = onClick,
            onClickComments = onClickComments,
            readLater = readLater,
            favorite = favorite
        )
    }
}
