package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.shared.components.comments.CommentItem
import it.devddk.hackernewsclient.viewmodels.ItemCollectionHolder
import it.devddk.hackernewsclient.viewmodels.NewsItemState
import it.devddk.hackernewsclient.viewmodels.NewsPageState
import kotlin.math.min

@Composable
fun MediumNewsRow(
    modifier: Modifier = Modifier,
    itemCollection: ItemCollectionHolder,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    toggleCollection: (Item, UserDefinedItemCollection) -> Unit,
) {
    val pageState by remember { itemCollection.pageState }.collectAsState(NewsPageState.Loading)
    val scrollState = rememberLazyListState()
    val itemListState by remember { itemCollection.itemListFlow }.collectAsState(
        initial = List(10) { i -> NewsItemState.Loading(-i) }
    )

    var initialApiCalled by rememberSaveable { mutableStateOf(false) }

    if (!initialApiCalled) {
        LaunchedEffect(Unit) {
            itemCollection.loadAll()
            initialApiCalled = true
        }
    }

    BoxWithConstraints {
        LazyRow(
            state = scrollState,
            modifier = modifier.fillMaxWidth(),
        ) {
            when (pageState) {
                is NewsPageState.NewsIdsError -> item {
                    LoadPageError()
                }
                is NewsPageState.Loading -> {
                    val length = min(10, itemListState.size)

                    itemsIndexed(itemListState.subList(0, length), key = { _, itemState -> itemState.itemId }) { index, _ ->
                        MediumNewsCard(
                            modifier = Modifier.width(352.dp),
                            toggleCollection = toggleCollection,
                            placeholder = true,
                        )

                        if (index != length - 1) {
                            Divider(
                                modifier = Modifier
                                    .alpha(0.1f)
                                    .height(192.dp)
                                    .padding(8.dp)
                                    .width(1.dp)
                            )
                        }
                    }
                }
                is NewsPageState.NewsIdsLoaded -> {
                    val length = min(10, itemListState.size)

                    itemsIndexed(itemListState.subList(0, length), key = { _, itemState -> itemState.itemId }) { index, itemState ->
                        when (itemState) {
                            is NewsItemState.Loading -> {
                                LaunchedEffect(itemState.itemId) {
                                    itemCollection.requestItem(itemState.itemId)
                                }

                                MediumNewsCard(
                                    modifier = Modifier.width(352.dp),
                                    toggleCollection = toggleCollection,
                                    placeholder = true,
                                )
                            }
                            is NewsItemState.ItemError -> LoadItemError()
                            is NewsItemState.ItemLoaded -> {
                                val item = itemState.item

                                if (item.type == ItemType.COMMENT) {
                                    CommentItem(
                                        item = item,
                                        modifier = Modifier.width(384.dp),
                                        maxLines = 4,
                                        onClick = { onItemClick(item) },
                                    )
                                } else {
                                    MediumNewsCard(
                                        item = item,
                                        modifier = Modifier.width(384.dp),
                                        onClick = { onItemClick(item) },
                                        onCommentClick = { onItemClickComments(item) },
                                        toggleCollection = toggleCollection,
                                    )
                                }
                            }
                        }

                        if (index != length - 1) {
                            Divider(
                                modifier = Modifier
                                    .alpha(0.1f)
                                    .height(192.dp)
                                    .padding(8.dp)
                                    .width(1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
