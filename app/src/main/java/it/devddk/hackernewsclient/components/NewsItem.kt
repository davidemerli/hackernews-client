package it.devddk.hackernewsclient.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsItem(item: Item) {
    val context = LocalContext.current
    val roundedShape = RoundedCornerShape(8.dp)

    val userString = remember(item) {
        val userId = item.by?.id
        if (userId != null) context.getString(R.string.author, userId)
        else context.getString(R.string.author_unknown)
    }

    val timeString = remember(item) {
        TimeDisplayUtils(context).toDateTimeAgoInterval(item.time)
    }

    val url = item.url
    val points = item.score
    val comments = item.descendants

    Card(modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .wrapContentHeight()
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 2.dp),
        shape = roundedShape
    ) {
        Row(Modifier
            .padding(4.dp)
            .height(IntrinsicSize.Min) // to allow for column with comment count to be aligned correctly to the bottom
        ) {
            Column(
                Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Cyan)
                    .fillMaxHeight()
                )
                Text(
                    "${points.toString()} pt",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
            Column(Modifier
                .padding(start = 8.dp, bottom = 8.dp)
                .fillMaxWidth(0.875f)) {
                url?.let {
                    Text(
                        getDomainName(url),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        // ellipsis ?
                        maxLines = 1
                    )
                }
                Text(
                    item.title ?: "title_fail",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                ),
                )
                Row {
                    Text(
                        "$userString - $timeString",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        // ellipsis ?
                        maxLines = 1
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "$comments",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(end = 6.dp, bottom = 5.dp)
                )
                // icon with width of 48dp
                Icon(
                    Icons.Filled.Email,
                    contentDescription = "Comments",
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}