package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun GoToLocationRow(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector ? = null,
    actionIcon: ImageVector = Icons.Filled.ArrowForward,
    location: String? = null,
    buttonText: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
    ) {
        leadingIcon?.let { Icon(leadingIcon, location, modifier = Modifier.padding(end = 8.dp)) }

        location?.let { Text(location) }

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onClick) {
            Text(buttonText, modifier = Modifier.padding(end = 4.dp))
            Icon(actionIcon, "Go to top stories")
        }
    }
}
