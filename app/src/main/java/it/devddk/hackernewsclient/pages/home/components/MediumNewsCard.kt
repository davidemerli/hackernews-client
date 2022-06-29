package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.shared.components.customPlaceholder
import it.devddk.hackernewsclient.shared.components.news.placeholderItem
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import java.net.URI

@Composable
fun MediumNewsCard(
    modifier: Modifier = Modifier,
    item: Item = placeholderItem,
    onClick: () -> Unit = {},
    placeholder: Boolean = false,
) {
    val context = LocalContext.current
    val domain = item.url?.let { URI(it).host }

    val authorString = context.getString(R.string.author, item.by)
    val timeString = remember(item) { TimeDisplayUtils(context).toDateTimeAgoInterval(item.time) }
    val scoreString = context.getString(R.string.points, item.score)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(192.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            item.previewUrl?.let { previewUrl ->
                Image(
                    painter = rememberAsyncImagePainter(
                        previewUrl,
                        imageLoader = ImageLoader.Builder(context)
                            .components {
                                add(SvgDecoder.Factory())
                            }
                            .build(),
                        contentScale = ContentScale.Crop
                    ),
                    contentDescription = "Image preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(96.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .customPlaceholder(visible = placeholder)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 2.dp).offset(y = (-4).dp)
            ) {
                domain?.let { domain ->
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, bottom = 2.dp)
                            .customPlaceholder(visible = placeholder)
                    )
                }

                Text(
                    text = item.title ?: context.getString(R.string.title_unknown),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
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

                        withStyle(MaterialTheme.typography.titleSmall.toSpanStyle()) {
                            append(" • $timeString")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .customPlaceholder(visible = placeholder),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp),
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
                modifier = Modifier.padding(horizontal = 8.dp).customPlaceholder(visible = placeholder),
            )

            Spacer(modifier = Modifier.weight(1f))

            item.descendants?.let { descendants ->
                TextButton(
                    onClick = { /*TODO*/ },
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

            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Share, "Share")
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.MoreVert, "More")
            }
        }
    }
}
