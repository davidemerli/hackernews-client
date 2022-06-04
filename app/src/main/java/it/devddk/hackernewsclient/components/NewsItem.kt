package it.devddk.hackernewsclient.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
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

val placeholderItem = Item(0, ItemType.STORY, by = null, time = null)

@Composable
@ExperimentalMaterial3Api
fun NewsItem(item: Item = placeholderItem, placeholder: Boolean, onClick: (() -> Unit) = {}) {
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

    val url = item.url
    val points = item.score
    val comments = item.descendants
    /*
    val painter =
        rememberAsyncImagePainter(
            item.previewUrl,
            filterQuality = FilterQuality.High,
            contentScale = ContentScale.Crop,
        )
    */

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = roundedShape,

        ) {
        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxWidth(0.1f)
                        .height(96.dp)
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    /*
                    Image(
                        painter = painter,
                        modifier = Modifier.fillMaxSize().placeholder(visible = placeholder),
                        contentDescription = "My content description",
                        contentScale = ContentScale.Crop
                    )*/
                }
                Text(
                    "${points.toString()} pt",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight(),
                    ) {
                        url?.let {
                            ClickableText(
                                text = AnnotatedString(getDomainName(url)),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic,
                                    textDecoration = TextDecoration.Underline,
                                ),
                                maxLines = 1,
                                onClick = {/*TODO*/ }
                            )
                        }
                        Text(
                            item.title?.trim() ?: R.string.title_unknown.toString(),
                            Modifier.placeholder(visible = placeholder),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 19.5.sp
                            ),
                        )
                    }
                    IconButton(
                        modifier = Modifier.offset(x = 10.dp, y = (-10).dp),
                        onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row {
                        ClickableText(
                            text = AnnotatedString(userString),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline,
                            ),
                            onClick = { /*TODO*/ }
                        )
                        Text(
                            " - $timeString",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    comments?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.offset(x = 10.dp, y = 10.dp),
                        ) {
                            Text(
                                "$comments",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                ),
                                modifier = Modifier.offset(x = 4.dp),
                            )
                            IconButton(
                                onClick = { /*TODO*/ }) {
                                Icon(
                                    Icons.Filled.Email,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
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
                Box(Modifier
                    .fillMaxWidth(0.8f)
                    .height(if (isAskHN) 90.dp else 110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                )
            }
            Column(Modifier.padding(start = 2.dp)) {
                IconButton(
                    onClick = { /*TODO*/ }) {
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
                        "${points.toString()} pt - 5 minutes read",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    IconButton(
                        onClick = { /*TODO*/ }) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)) {
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