package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.lifecycle.viewmodel.compose.viewModel
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.favorite
import it.devddk.hackernewsclient.domain.model.items.readLater
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun SwipeableNewsItem(
    item: Item = placeholderItem,
    placeholder: Boolean = false,
    onClick: () -> Unit = {},
    onClickComments: () -> Unit = {},
) {
    val readLater = remember { mutableStateOf(item.collections.readLater) }
    val favorite = remember { mutableStateOf(item.collections.favorite) }

    val coroutineScope = rememberCoroutineScope()
    val viewModel: HomePageViewModel = viewModel()

    val readLaterAction = SwipeAction(
        icon = rememberVectorPainter(if (!readLater.value) Icons.Filled.Update else Icons.Filled.UpdateDisabled),
        background = Color.Green,
        onSwipe = {
            onCollectionToggle(
                itemId = item.id,
                toggleable = readLater,
                collection = UserDefinedItemCollection.ReadLater,
                coroutineScope = coroutineScope,
                viewModel = viewModel,
            )
        }
    )

    val favoriteAction = SwipeAction(
        icon = rememberVectorPainter(if (!favorite.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder),
        background = Color.Red,
        isUndo = true,
        onSwipe = {
            onCollectionToggle(
                itemId = item.id,
                toggleable = favorite,
                collection = UserDefinedItemCollection.Favorites,
                coroutineScope = coroutineScope,
                viewModel = viewModel,
            )
        },
    )

    SwipeableActionsBox(
        startActions = listOf(readLaterAction),
        endActions = listOf(favoriteAction),
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
