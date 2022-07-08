package it.devddk.hackernewsclient.shared.components.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.favorite
import it.devddk.hackernewsclient.domain.model.items.readLater
import it.devddk.hackernewsclient.domain.model.search.SearchResultMetaData
import it.devddk.hackernewsclient.pages.parseHTML
import it.devddk.hackernewsclient.shared.components.customPlaceholder
import it.devddk.hackernewsclient.shared.components.news.ItemColorHint
import it.devddk.hackernewsclient.utils.TimeDisplayUtils

@Composable
@OptIn(ExperimentalTextApi::class)
fun CommentItem(
    modifier: Modifier = Modifier,
    item: Item,
    searchMetaData: SearchResultMetaData? = null,
    onClick: () -> Unit,
    placeholder: Boolean = false,
    favorite: MutableState<Boolean> = remember { mutableStateOf(item.collections.favorite) },
    readLater: MutableState<Boolean> = remember { mutableStateOf(item.collections.readLater) },
) {
    val context = LocalContext.current

    val timeString = remember(item) { TimeDisplayUtils(context).toDateTimeAgoInterval(item.time) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        ItemColorHint(color = MaterialTheme.colorScheme.tertiary)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!placeholder && favorite.value) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 1.dp)
                    )
                }

                if (!placeholder && readLater.value) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = "Read later",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 1.dp)
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.tertiary
                            ).toSpanStyle()
                        ) {
                            append("${item.by}")
                        }

                        withStyle(
                            MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ).toSpanStyle()
                        ) {
                            append(" on ")
                        }
                    },
                    modifier = Modifier.customPlaceholder(placeholder)
                )
            }
            Text(
                text = "${searchMetaData?.storyTitle ?: item.storyId}" + " ".repeat(30),
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .customPlaceholder(placeholder),
            )

            Text(
                text = "${item.text?.parseHTML() ?: "no_text"}\n\n\n\n\n",
                style = MaterialTheme.typography.bodyLarge.copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        ),
                    ),
                ),
                maxLines = 5,
                modifier = Modifier.customPlaceholder(placeholder),
            )

            Text(timeString, style = MaterialTheme.typography.bodyMedium)
        }
    }
}