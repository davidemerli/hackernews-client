package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.shared.components.customPlaceholder

@Composable
fun NewsItemComments(
    modifier: Modifier = Modifier,
    placeholder: Boolean = false,
    onClick: () -> Unit,
    commentsNumber: Int
) {
    if (!placeholder) {
        TextButton(
            modifier = modifier,
            onClick = onClick
        ) {
            Icon(
                Icons.Filled.Email,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "$commentsNumber",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .offset(x = 4.dp)
                    .customPlaceholder(visible = placeholder),
            )
        }
    }
}