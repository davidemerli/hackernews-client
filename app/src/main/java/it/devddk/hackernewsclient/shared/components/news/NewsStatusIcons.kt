package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewsStatusIcons(
    modifier: Modifier = Modifier,
    favorite: Boolean,
    readLater: Boolean,
    placeholder: Boolean = false,
) {
    Row(
        modifier = modifier,
    ) {
        if (!placeholder && favorite) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 1.dp)
            )
        }

        if (!placeholder && readLater) {
            Icon(
                Icons.Filled.Update,
                contentDescription = "Read later",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 1.dp)
            )
        }
    }
}
