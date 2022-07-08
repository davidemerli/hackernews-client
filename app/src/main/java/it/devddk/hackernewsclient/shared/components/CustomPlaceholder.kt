package it.devddk.hackernewsclient.shared.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.material.fadeHighlightColor
import com.google.accompanist.placeholder.placeholder

fun Modifier.customPlaceholder(visible: Boolean = false) = composed {
    this
        .placeholder(
            visible = visible,
            color = PlaceholderDefaults.fadeHighlightColor(alpha = 0.2f),
            shape = RoundedCornerShape(2.dp)
        )
}
