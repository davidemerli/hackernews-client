package it.devddk.hackernewsclient.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import timber.log.Timber
import java.net.URI

fun getDomainName(url: String): String {
    return try {
        val uri = URI(url)
        val domain: String = uri.host

        domain.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}

// enum with subtypes
enum class ItemSubtype(val value: String) {
    ASK_HN("Ask HN"),
    SHOW_HN("Show HN"),
    TELL_HN("Tell HN")
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

val placeholderItem = Item(
    0,
    ItemType.STORY,
    title = "_".repeat(30),
    url = "https://news.ycombinator.com/",
    by = "_".repeat(10),
    time = null
)

@Composable
fun ColorHint(item: Item) {
    val palette: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }

    val backgroundColor = item.subtype()?.let {
        palette[it.ordinal]
    } ?: MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .width(8.dp)
            .fillMaxHeight()
            .background(backgroundColor)
    )
}

@Composable
fun ItemDomain(item: Item, placeholder: Boolean = false) {
    item.url?.let { url ->
        val domain = getDomainName(url)

        ClickableText(
            text = AnnotatedString(domain),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                textDecoration = TextDecoration.Underline,
            ),
            maxLines = 1,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .placeholder(
                    visible = placeholder,
                    color = PlaceholderDefaults.color(contentAlpha = 0.5f),
                    highlight = PlaceholderHighlight.fade(),
                ),
            onClick = { /*TODO*/ },
        )
    }
}

@Composable
fun ItemTitle(title: String, placeholder: Boolean = false) {
    Text(
        title.trim(),
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.5.sp
        ),
        modifier = Modifier.placeholder(
            visible = placeholder,
            color = PlaceholderDefaults.color(contentAlpha = 0.5f),
            highlight = PlaceholderHighlight.fade(),
        ),
    )
}

@Composable
fun OptionsButton(item: Item, placeholder: Boolean = false) {
    IconButton(
        modifier = Modifier.offset(x = 10.dp, y = (-10).dp),
        onClick = { /*TODO*/ }
    ) {
        Icon(
            Icons.Filled.MoreVert,
            contentDescription = "Options",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.placeholder(
                visible = placeholder,
                color = PlaceholderDefaults.color(contentAlpha = 0.5f),
                highlight = PlaceholderHighlight.fade(),
            )
        )
    }
}

@Composable
fun ItemBy(item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current

    val userString = remember(item) {
        when (item.by) {
            null -> context.getString(R.string.author_unknown)
            else -> context.getString(R.string.author, item.by)
        }
    }

    ClickableText(
        text = AnnotatedString(userString),
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = Modifier.placeholder(
            visible = placeholder,
            color = PlaceholderDefaults.color(contentAlpha = 0.5f),
            highlight = PlaceholderHighlight.fade(),
        ),
        onClick = { /*TODO*/ }
    )
}

@Composable
fun ItemPoints(item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current

    val pointsString = remember(item) {
        context.getString(R.string.points, item.score)
    }

    Timber.d("${item.score}")

    Text(
        pointsString,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.placeholder(
            visible = placeholder,
            color = PlaceholderDefaults.color(contentAlpha = 0.5f),
            highlight = PlaceholderHighlight.fade(),
        ),
    )
}

@Composable
fun ItemTime(item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current

    val timeString = remember(item) { TimeDisplayUtils(context).toDateTimeAgoInterval(item.time) }

    Text(
        timeString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.placeholder(
            visible = placeholder,
            color = PlaceholderDefaults.color(contentAlpha = 0.5f),
            highlight = PlaceholderHighlight.fade(),
        ),
    )
}

@Composable
fun ItemComments(item: Item, placeholder: Boolean = false) {
    item.descendants?.let { comments ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(x = 10.dp, y = 10.dp),
        ) {
            Text(
                "$comments",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier
                    .offset(x = 4.dp)
                    .placeholder(
                        visible = placeholder,
                        color = PlaceholderDefaults.color(contentAlpha = 0.5f),
                        highlight = PlaceholderHighlight.fade(),
                    ),
            )
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.placeholder(
                        visible = placeholder,
                        color = PlaceholderDefaults.color(contentAlpha = 0.5f),
                        highlight = PlaceholderHighlight.fade(),
                    )
                )
            }
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun NewsItem(item: Item = placeholderItem, placeholder: Boolean, onClick: (() -> Unit) = {}) {
    val roundedShape = RoundedCornerShape(16.dp)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = roundedShape,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(2.dp),
    ) {
        Row(
            Modifier
                .padding(10.dp)
                .height(IntrinsicSize.Max)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ColorHint(item = item)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        ItemDomain(item, placeholder = placeholder)

                        ItemTitle(
                            title = item.title ?: R.string.title_unknown.toString(),
                            placeholder = placeholder
                        )
                    }
                    OptionsButton(item, placeholder = placeholder)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row {
                        ItemPoints(item, placeholder = placeholder)

                        Text(
                            text = " - ",

                        )

                        ItemBy(item, placeholder = placeholder)

                        Text(
                            text = " - ",
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        ItemTime(item, placeholder = placeholder)
                    }

                    ItemComments(item, placeholder = placeholder)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NewsItemTall(item: Item) {
    val context = LocalContext.current
    val roundedShape = RoundedCornerShape(16.dp)

    val userString = remember(item) {
        val userId = item.by
        if (userId != null) context.getString(R.string.author, userId)
        else context.getString(R.string.author_unknown)
    }

    val timeString = remember(item) {
        TimeDisplayUtils(context).toDateTimeAgoInterval(item.time)
    }

    val title = item.title?.replace("Ask HN:", "")?.trim()
    val url = item.url
    val points = item.score
    val comments = item.descendants

    val isAskHN = item.title?.contains("Ask HN") ?: false

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        shape = roundedShape
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Column {
                url?.let {
                    Text(
                        getDomainName(url),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)
                    )
                }
                if (isAskHN) {
                    Box(Modifier.padding(vertical = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                "Ask HN",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth(0.8f)
                        .height(if (isAskHN) 90.dp else 110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
            Column(Modifier.padding(start = 2.dp)) {
                IconButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "More",
                    )
                }
                IconButton(
                    onClick = { /*TODO*/ },
                    Modifier.offset(y = -(4).dp)
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "More",
                    )
                }
                IconButton(
                    onClick = { /*TODO*/ },
                    Modifier.offset(y = -(8).dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More",
                    )
                }
            }
        }
        Column(
            Modifier.padding(start = 10.dp)
        ) {
            Text(
                title ?: "title_fail",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        "$userString - $timeString",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        "$points pt - 5 minutes read",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    IconButton(
                        onClick = { /*TODO*/ }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                "$comments",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.MailOutline,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
