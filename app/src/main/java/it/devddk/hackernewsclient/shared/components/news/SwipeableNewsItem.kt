package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.favorite
import it.devddk.hackernewsclient.domain.model.items.readLater
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import me.saket.swipe.rememberSwipeableActionsState
import timber.log.Timber

@Composable
fun SwipeableNewsItem(
    item: Item = placeholderItem,
    placeholder: Boolean = false,
    onClick: () -> Unit = {},
    onClickComments: () -> Unit = {},
    toggleCollection: (Item, UserDefinedItemCollection) -> Unit = { _, _ -> },
) {
    Timber.d("title = ${item.title}")
    Timber.d("readLater = ${item.collections.readLater}")
    Timber.d("favorite = ${item.collections.favorite}")

    val readLater = remember { mutableStateOf(item.collections.readLater) }
    val favorite = remember { mutableStateOf(item.collections.favorite) }

    val swipeState = rememberSwipeableActionsState()

    val tintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    val readLaterAction = SwipeAction(
        icon = rememberVectorPainter(
            if (!readLater.value) Icons.Filled.Update else Icons.Filled.UpdateDisabled,
            tintColor = tintColor
        ),
        background = MaterialTheme.colorScheme.tertiary,
        onSwipe = {
            readLater.value = !readLater.value
            toggleCollection(item, UserDefinedItemCollection.ReadLater)
        }
    )

    val favoriteAction = SwipeAction(
        icon = rememberVectorPainter(
            if (!favorite.value) Icons.Filled.Star else Icons.Filled.StarOutline,
            tintColor = tintColor
        ),
        background = MaterialTheme.colorScheme.primary,
        onSwipe = {
            favorite.value = !favorite.value
            toggleCollection(item, UserDefinedItemCollection.Favorites)
        },
    )

    SwipeableActionsBox(
        state = swipeState,
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

@Composable
fun rememberVectorPainter(image: ImageVector, tintColor: Color) =
    rememberVectorPainter(
        defaultWidth = image.defaultWidth,
        defaultHeight = image.defaultHeight,
        viewportWidth = image.viewportWidth,
        viewportHeight = image.viewportHeight,
        name = image.name,
        tintColor = tintColor,
        tintBlendMode = image.tintBlendMode,
        autoMirror = image.autoMirror,
        content = { _, _ -> RenderVectorGroup(group = image.root) }
    )
