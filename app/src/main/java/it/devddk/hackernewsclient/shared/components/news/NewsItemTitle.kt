package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.shared.components.customPlaceholder

@Composable
fun NewsItemTitle(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: Boolean = false
) {
    Text(
        title.trim(),
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.5.sp
        ),
        modifier = modifier.customPlaceholder(visible = placeholder)
    )
}
