package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.shared.components.customPlaceholder

@Composable
fun NewsOptionsButton(
    modifier: Modifier = Modifier,
    favorite: Boolean,
    readLater: Boolean,
    onFavoriteClick: () -> Unit,
    onReadLaterClick: () -> Unit,
    onShareUrl: (() -> Unit)? = null,
    onShareHNLink: (() -> Unit)? = null,
    placeholder: Boolean = false,
) {
    if (!placeholder) {
        var expanded by remember { mutableStateOf(false) }

        IconButton(
            modifier = modifier.offset(x = 10.dp, y = -(10).dp),
            onClick = { expanded = !expanded },
        ) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.customPlaceholder(visible = placeholder)
            )

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AddToFavorite(favorite = favorite, onClick = { onFavoriteClick(); expanded = false })
                AddToReadLater(readLater = readLater, onClick = { onReadLaterClick(); expanded = false })

                onShareUrl?.let { ShareArticle(onClick = { it(); expanded = false }) }
                onShareHNLink?.let { ShareHNLink(onClick = { it(); expanded = false }) }
            }
        }
    }
}

@Composable
fun AddToFavorite(
    favorite: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(if (!favorite) "Add to favorites" else "Remove from favorites") },
        leadingIcon = {
            Icon(
                if (!favorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = "Favorite",
            )
        },
        onClick = onClick,
    )
}

@Composable
fun AddToReadLater(
    readLater: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(if (!readLater) "Read later" else "Remove from read later") },
        leadingIcon = {
            Icon(
                if (!readLater) Icons.Filled.BookmarkAdd else Icons.Filled.BookmarkRemove,
                contentDescription = "Read later",
            )
        },
        onClick = onClick,
    )
}

@Composable
fun ShareArticle(
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text("Share Article") },
        leadingIcon = {
            Icon(Icons.Filled.Share, contentDescription = "Share Article")
        },
        onClick = onClick,
    )
}

@Composable
fun ShareHNLink(
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text("Share HN link") },
        leadingIcon = {
            Icon(Icons.Filled.Share, contentDescription = "Share HN link")
        },
        onClick = onClick,
    )
}
