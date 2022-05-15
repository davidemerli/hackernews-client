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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NewsItem(item: Item, onClick : (() -> Unit)? = null) {
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

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .wrapContentHeight()
            .fillMaxWidth()
            .clickable { onClick?.let { it() } }
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = roundedShape,

        ) {
        Row(
            Modifier
                .padding(10.dp)
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    Modifier
                        .width(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxHeight(0.8f)
                        .background(MaterialTheme.colorScheme.secondary)
                )
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
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.85f),
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
                            "${item.title}",
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
                            onClick = {/*TODO*/ }
                        )
                        Text(
                            " - $timeString",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoadingItem() {
    val context = LocalContext.current
    val roundedShape = RoundedCornerShape(16.dp)

    val userString = "Loading..."

    val timeString = "Bho"

    val url = "www.loading.com"
    val points = 0
    val comments = 0

    Card(
        modifier = Modifier
            .background(color = Color.Green)
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = roundedShape,

        ) {
        Row(
            Modifier
                .padding(10.dp)
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxHeight()
                            .background(Color.Red)
                    )
                    Text(
                        "$points pt",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.875f)
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "lading item",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 19.5.sp
                            ),
                        )
                        Text(
                            getDomainName(url),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            // ellipsis ?
                        )
                    }
                    Text(
                        "$userString - $timeString",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End) {
                    Text(
                        "$comments",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ErrorItem() {
    val context = LocalContext.current
    val roundedShape = RoundedCornerShape(16.dp)

    val userString = "Error..."

    val timeString = "Bho"

    val url = "www.loading.com"
    val points = 0
    val comments = 0

    Card(
        modifier = Modifier
            .background(color = Color.Yellow)
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = roundedShape,

        ) {
        Row(
            Modifier
                .padding(10.dp)
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxHeight()
                            .background(Color.Red)
                    )
                    Text(
                        "$points pt",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.875f)
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "lading item",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 19.5.sp
                            ),
                        )
                        Text(
                            getDomainName(url),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            // ellipsis ?
                        )
                    }
                    Text(
                        "$userString - $timeString",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End) {
                    Text(
                        "$comments",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
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