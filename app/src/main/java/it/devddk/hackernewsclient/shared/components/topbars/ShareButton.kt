package it.devddk.hackernewsclient.shared.components.topbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.devddk.hackernewsclient.shared.components.news.ShareArticle
import it.devddk.hackernewsclient.shared.components.news.ShareHNLink
import it.devddk.hackernewsclient.shared.components.news.shareStringContent

@Composable
fun ShareButton(
    modifier: Modifier = Modifier,
    itemUrl: String?,
    hnUrl: String,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        modifier = modifier,
        onClick = { expanded = !expanded },
    ) {
        Icon(
            Icons.Filled.Share,
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            itemUrl?.let {
                ShareArticle(onClick = { shareStringContent(context, it); expanded = false })
            }

            ShareHNLink(onClick = { shareStringContent(context, hnUrl); expanded = false })
        }
    }
}
