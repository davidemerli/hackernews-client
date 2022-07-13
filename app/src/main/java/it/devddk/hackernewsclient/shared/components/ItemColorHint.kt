package it.devddk.hackernewsclient.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ItemColorHint(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
) {
    Box(
        modifier = modifier
            .width(8.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
    )
}
