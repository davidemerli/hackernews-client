package it.devddk.hackernewsclient.shared.components.news

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.items.favorite
import it.devddk.hackernewsclient.domain.model.items.readLater
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.pages.parseHTML
import it.devddk.hackernewsclient.viewmodels.NewsListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URI

@Composable
fun NewsItem(
    item: Item = placeholderItem,
    placeholder: Boolean = false,
    onClick: () -> Unit = {},
    onClickComments: () -> Unit = {},
    favorite: MutableState<Boolean> = remember { mutableStateOf(item.collections.favorite) },
    readLater: MutableState<Boolean> = remember { mutableStateOf(item.collections.readLater) },
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val viewModel: NewsListViewModel = viewModel()

    val itemDomain = remember { item.url?.let { getDomainName(it) } }

    val palette: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }
    val primaryColor = MaterialTheme.colorScheme.primary

    val hintColor = remember {
        item.subtype()?.let {
            palette[it.ordinal]
        } ?: primaryColor
    }

    ConstraintLayout(
        modifier = Modifier
            .height(128.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        val (statusIcons, colorHint, domain, title) = createRefs()
        val (comments, more) = createRefs()

        val (pt, by, time) = createRefs()

        NewsColorHint(
            color = hintColor,
            modifier = Modifier
                .constrainAs(colorHint) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }
        )

        itemDomain?.let {
            NewsItemDomain(
                domain = it,
                placeholder = placeholder,
                modifier = Modifier
                    .constrainAs(domain) {
                        top.linkTo(parent.top)
                        start.linkTo(statusIcons.end, margin = 2.dp)

                        if (favorite.value or readLater.value) {
                            bottom.linkTo(statusIcons.bottom)
                        }
                    }
            )
        }

        NewsStatusIcons(
            favorite = favorite.value,
            readLater = readLater.value,
            placeholder = placeholder,
            modifier = Modifier
                .constrainAs(statusIcons) {
                    top.linkTo(parent.top)
                    start.linkTo(colorHint.end, margin = 4.dp)
                }
                .offset(y = -(1).dp)
        )

        item.title?.let {
            NewsItemTitle(
                title = it.parseHTML(),
                placeholder = placeholder,
                modifier = Modifier
                    .padding(end = 56.dp) // to avoid overlapping with the dropdown menu
                    .constrainAs(title) {
                        top.linkTo(domain.bottom, margin = 2.dp)
                        start.linkTo(colorHint.end, margin = 6.dp)
                    }
            )
        }

        item.score?.let {
            NewsItemPoints(
                score = it,
                placeholder = placeholder,
                modifier = Modifier.constrainAs(pt) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(colorHint.end, margin = 6.dp)
                }
            )
        }

        NewsItemAuthor(
            author = item.by,
            placeholder = placeholder,
            modifier = Modifier.constrainAs(by) {
                bottom.linkTo(parent.bottom)
                start.linkTo(pt.end, margin = 6.dp)
            }
        )

        item.time?.let {
            NewsItemTime(
                time = it,
                placeholder = placeholder,
                modifier = Modifier.constrainAs(time) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(by.end, margin = 6.dp)
                }
            )
        }

        NewsOptionsButton(
            placeholder = placeholder,
            favorite = favorite.value,
            readLater = readLater.value,
            onShareHNLink = {
                shareStringContent(context, "https://news.ycombinator.com/item?id=${item.id}")
            },
            onShareUrl = {
                item.url?.let { url ->
                    shareStringContent(context, url)
                }
            },
            onFavoriteClick = {
                onCollectionToggle(
                    itemId = item.id,
                    toggleable = favorite,
                    collection = UserDefinedItemCollection.Favorites,
                    coroutineScope = coroutineScope,
                    viewModel = viewModel,
                )
            },
            onReadLaterClick = {
                onCollectionToggle(
                    itemId = item.id,
                    toggleable = readLater,
                    collection = UserDefinedItemCollection.ReadLater,
                    coroutineScope = coroutineScope,
                    viewModel = viewModel,
                )
            },
            modifier = Modifier.constrainAs(comments) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            }
        )

        item.descendants?.let {
            NewsItemComments(
                commentsNumber = it,
                onClick = onClickComments,
                placeholder = placeholder,
                modifier = Modifier.constrainAs(more) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                },
            )
        }
    }
}

fun shareStringContent(context: Context, content: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(context, shareIntent, null)
}

fun onCollectionToggle(
    coroutineScope: CoroutineScope,
    viewModel: NewsListViewModel,
    itemId: ItemId,
    toggleable: MutableState<Boolean>,
    collection: UserDefinedItemCollection,
) {
    toggleable.value = !toggleable.value

    coroutineScope.launch {
        if (toggleable.value) {
            viewModel.addToFavorites(itemId, collection)
        } else {
            viewModel.removeFromFavorites(itemId, collection)
        }
    }
}

// enum with subtypes
enum class ItemSubtype(val value: String) {
    ASK_HN("Ask HN"), SHOW_HN("Show HN"), TELL_HN("Tell HN")
}

fun Item.subtype(): ItemSubtype? {
    if (title != null) {
        if (title!!.startsWith("Ask HN")) {
            return ItemSubtype.ASK_HN
        } else if (title!!.startsWith("Show HN")) {
            return ItemSubtype.SHOW_HN
        } else if (title!!.startsWith("Tell HN")) {
            return ItemSubtype.TELL_HN
        }
    }

    return null
}

internal val placeholderItem = Item(
    0,
    ItemType.STORY,
    title = "_".repeat(30),
    url = "https://news.ycombinator.com/",
    by = "_".repeat(10),
    score = 100,
    descendants = 100,
    time = null
)

fun getDomainName(url: String): String {
    return try {
        val uri = URI(url)
        val domain: String = uri.host

        domain.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}
