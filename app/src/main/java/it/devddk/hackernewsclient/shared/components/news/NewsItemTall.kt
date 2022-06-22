package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.utils.TimeDisplayUtils

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Deprecated("Needs to be completely rewritten")
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
        Row(modifier = Modifier.padding(start = 8.dp)) {
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
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimary),
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
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Column(Modifier.padding(start = 2.dp)) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "More",
                    )
                }
                IconButton(onClick = { /*TODO*/ }, Modifier.offset(y = -(4).dp)) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "More",
                )
            }
                IconButton(onClick = { /*TODO*/ }, Modifier.offset(y = -(8).dp)) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "More",
                )
            }
            }
        }
        Column(Modifier.padding(start = 10.dp)) {
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
                    IconButton(onClick = { /*TODO*/ }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                "$comments",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.MailOutline,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
