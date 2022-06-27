package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.shared.components.LoadingIndicatorCircular
import it.devddk.hackernewsclient.viewmodels.ItemCollectionHolder
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.NewsPageState

@Composable
fun NewsRow(
    modifier: Modifier = Modifier,
    itemCollection: ItemCollectionHolder,
    onItemClick: (ItemId) -> (() -> Unit),
) {
    val pageState by remember { itemCollection.pageState }.collectAsState(NewsPageState.Loading)
    val itemListState by remember { itemCollection.itemListFlow }.collectAsState(initial = emptyList())

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
    ) {
        when (pageState) {
            is NewsPageState.Loading -> LoadingPage(itemCollection = itemCollection)
            is NewsPageState.NewsIdsError -> LoadPageError()
            is NewsPageState.NewsIdsLoaded -> {
                itemListState.subList(0, 10).forEach { itemState ->
                    when (itemState) {
                        is NewsItemState.Loading -> LoadingItem(
                            itemCollection = itemCollection,
                            itemState = itemState
                        )
                        is NewsItemState.ItemError -> LoadItemError()
                        is NewsItemState.ItemLoaded -> {
                            val item = itemState.item

                            TallNewsCard(
                                item = item,
                                modifier = Modifier.width(352.dp),
                                onClick = onItemClick(item.id)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RowScope.LoadingPage(
    modifier: Modifier = Modifier,
    itemCollection: ItemCollectionHolder
) {
    LoadingIndicatorCircular(modifier = modifier.weight(1f))

    LaunchedEffect(Unit) { itemCollection.loadAll() }
}

@Composable
internal fun LoadPageError() {
    // TODO: improve this
    Text("Error loading items")
}

@Composable
internal fun LoadingItem(
    modifier: Modifier = Modifier,
    itemCollection: ItemCollectionHolder,
    itemState: NewsItemState,
) {
    LoadingIndicatorCircular(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1.0f)
    )

    LaunchedEffect(itemState.itemId) {
        itemCollection.requestItem(itemState.itemId)
    }
}

@Composable
internal fun LoadItemError() {
    // TODO: improve this
    Text("Error loading item")
}
