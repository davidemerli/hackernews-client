package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.shared.components.LoadingIndicatorCircular
import it.devddk.hackernewsclient.shared.components.news.NewsItem
import it.devddk.hackernewsclient.shared.components.news.SwipeableNewsItem
import it.devddk.hackernewsclient.viewmodels.ItemCollectionHolder
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.NewsPageState
import kotlin.math.min

@Composable
fun ColumnScope.NewsColumn(
    modifier: Modifier = Modifier,
    itemCollection: ItemCollectionHolder,
    onItemClick: (Item) -> Unit,
    toggleCollection: (Item, UserDefinedItemCollection) -> Unit,
) {
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    val pageState by remember { itemCollection.pageState }.collectAsState(if (hasLoaded) NewsPageState.NewsIdsLoaded(emptyList()) else NewsPageState.Loading)
    val itemListState by remember { itemCollection.itemListFlow }.collectAsState(initial = emptyList())

    var initialApiCalled by rememberSaveable { mutableStateOf(false) }

    if (!initialApiCalled && !hasLoaded) {
        LaunchedEffect(Unit) {
            itemCollection.loadAll()
            initialApiCalled = true
        }
    }

    when (pageState) {
        is NewsPageState.Loading -> LoadingPage(modifier, itemCollection = itemCollection)

        is NewsPageState.NewsIdsError -> LoadPageError()

        is NewsPageState.NewsIdsLoaded -> {
            hasLoaded = true

            val length = min(10, itemListState.size)

            itemListState.subList(0, length).forEach { itemState ->
                when (itemState) {
                    is NewsItemState.Loading -> {
                        LaunchedEffect(itemState.itemId) {
                            itemCollection.requestItem(itemState.itemId)
                        }

                        NewsItem(placeholder = true)
                    }
                    is NewsItemState.ItemError -> LoadItemError()
                    is NewsItemState.ItemLoaded -> {
                        val item = itemState.item

                        SwipeableNewsItem(
                            item = item,
                            onClick = { onItemClick(item) },
                            toggleCollection = toggleCollection,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun LoadingPage(
    modifier: Modifier = Modifier,
    itemCollection: ItemCollectionHolder
) {
    LoadingIndicatorCircular(modifier = modifier.fillMaxSize())
}
