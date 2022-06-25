package it.devddk.hackernewsclient.shared.components.topbars

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.devddk.hackernewsclient.shared.components.openInBrowser

@Composable
fun OpenInBrowserButton(
    itemUrl: String?,
    hnUrl: String,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Filled.OpenInNew, "Open in browser")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            itemUrl?.let { url ->
                DropdownMenuItem(
                    text = { Text("Open Article in browser") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.OpenInBrowser,
                            contentDescription = "Open Article in browser"
                        )
                    },
                    onClick = { openInBrowser(context, url); expanded = false },
                )
            }
            DropdownMenuItem(
                text = { Text("Open HN link in browser") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.OpenInBrowser,
                        contentDescription = "Open HN link in browser"
                    )
                },
                onClick = { openInBrowser(context, hnUrl); expanded = false },
            )
        }
    }
}
