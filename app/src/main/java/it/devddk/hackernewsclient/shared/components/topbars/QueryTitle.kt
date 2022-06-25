package it.devddk.hackernewsclient.shared.components.topbars

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.devddk.hackernewsclient.shared.components.ROUTE_ICONS
import it.devddk.hackernewsclient.shared.components.ROUTE_TITLES

@Composable
fun QueryTitle(query: String) {
    Row {
        Icon(
            imageVector = ROUTE_ICONS[query]!!,
            contentDescription = query,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 6.dp)
        )

        Text(ROUTE_TITLES[query] ?: query)
    }
}
