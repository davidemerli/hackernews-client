package it.devddk.hackernewsclient.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.utils.TimeDisplayUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsItem(item: Item) {
    val context = LocalContext.current
    val roundedShape = RoundedCornerShape(4.dp)

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
        .fillMaxWidth(),
        shape = roundedShape
    ) {
        Row {
            Box(Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Green)
            )
            Column {
                Text(
                    url ?: "url_fail",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                    // ellipsis ?
                    maxLines = 1
                )
                Text(
                    item.title ?: "title_fail",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    userString,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                    // ellipsis ?
                    maxLines = 1
                )
            }

        }
    }
}