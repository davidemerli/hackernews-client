package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import com.google.accompanist.placeholder.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.favorite
import it.devddk.hackernewsclient.domain.model.items.readLater
import it.devddk.hackernewsclient.shared.components.customPlaceholder
import it.devddk.hackernewsclient.shared.components.news.AddToFavorite
import it.devddk.hackernewsclient.shared.components.news.AddToReadLater
import it.devddk.hackernewsclient.shared.components.news.NewsStatusIcons
import it.devddk.hackernewsclient.shared.components.news.ShareArticle
import it.devddk.hackernewsclient.shared.components.news.ShareHNLink
import it.devddk.hackernewsclient.shared.components.news.placeholderItem
import it.devddk.hackernewsclient.shared.components.news.shareStringContent
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import java.net.URI

@Composable
fun TallNewsCard(
    modifier: Modifier = Modifier,
    item: Item = placeholderItem,
    onClick: () -> Unit = {},
    onClickComments: () -> Unit = {},
    toggleCollection: (Item, UserDefinedItemCollection) -> Unit,
    placeholder: Boolean = false,
) {
    val context = LocalContext.current
    val domain = item.url?.let { URI(it).host }

    val authorString = context.getString(R.string.author, item.by)
    val timeString = remember(item) { TimeDisplayUtils(context).toDateTimeAgoInterval(item.time) }
    val scoreString = context.getString(R.string.points, item.score)

    val imagePainter = rememberAsyncImagePainter(
        item.previewUrl,
        imageLoader = ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build(),
        contentScale = ContentScale.Crop
    )

    var shareExpanded by remember { mutableStateOf(false) }
    var moreExpanded by remember { mutableStateOf(false) }

    var favorite by rememberSaveable { mutableStateOf(item.collections.favorite) }
    var readLater by rememberSaveable { mutableStateOf(item.collections.readLater) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .height(352.dp)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "Image preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .padding(bottom = 4.dp)
                .customPlaceholder(visible = placeholder)
                .clip(RoundedCornerShape(16.dp))
        )

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            NewsStatusIcons(
                favorite = favorite,
                readLater = readLater,
                modifier = Modifier.padding(end = if (favorite or readLater) 8.dp else 0.dp)
            )

            domain?.let { domain ->
                Text(
                    text = domain,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .customPlaceholder(visible = placeholder)
                )
            }
        }

        Text(
            text = item.title ?: context.getString(R.string.title_unknown),
            style = MaterialTheme.typography.titleMedium.copy(
                lineHeight = 18.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
                .customPlaceholder(visible = placeholder),
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.tertiary
                    ).toSpanStyle(),
                ) {
                    append(authorString)
                }

                withStyle(MaterialTheme.typography.titleSmall.toSpanStyle(),) {
                    append(" • $timeString")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
                .customPlaceholder(visible = placeholder),
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ).toSpanStyle(),
                    ) {
                        append(scoreString)
                    }
                },
                modifier = Modifier.customPlaceholder(visible = placeholder),
            )

            Spacer(modifier = Modifier.weight(1f))

            item.descendants?.let { descendants ->
                TextButton(
                    onClick = onClickComments,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Text(
                        "$descendants",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .customPlaceholder(visible = placeholder)
                    )
                    Icon(Icons.Filled.Chat, "Comments")
                }
            }

            IconButton(onClick = { shareExpanded = !shareExpanded }) {
                Icon(Icons.Filled.Share, "Share")

                DropdownMenu(
                    expanded = shareExpanded,
                    onDismissRequest = { shareExpanded = false }
                ) {
                    item.url?.let { url ->
                        ShareArticle(
                            onClick = {
                                shareStringContent(context, url)
                                shareExpanded = false
                            }
                        )
                    }
                    ShareHNLink(onClick = {
                        shareStringContent(context, "https://news.ycombinator.com/item?id=${item.id}")
                        shareExpanded = false
                    })
                }
            }

            IconButton(onClick = { moreExpanded = !moreExpanded }) {
                Icon(Icons.Filled.MoreVert, "More")

                DropdownMenu(
                    expanded = moreExpanded,
                    onDismissRequest = { moreExpanded = false }
                ) {
                    AddToFavorite(
                        favorite = favorite,
                        onClick = {
                            favorite = !favorite
                            moreExpanded = false

                            toggleCollection(item, UserDefinedItemCollection.Favorites)
                        }
                    )

                    AddToReadLater(
                        readLater = readLater,
                        onClick = {
                            readLater = !readLater
                            moreExpanded = false

                            toggleCollection(item, UserDefinedItemCollection.ReadLater)
                        }
                    )
                }
            }
        }
    }
}
