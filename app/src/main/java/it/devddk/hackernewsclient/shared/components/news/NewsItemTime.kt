package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.shared.components.customPlaceholder
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import java.time.LocalDateTime

@Composable
fun NewsItemTime(
    modifier: Modifier = Modifier,
    time: LocalDateTime,
    placeholder: Boolean = false
) {
    val context = LocalContext.current

    val timeString = remember { TimeDisplayUtils(context).toDateTimeAgoInterval(time) }

    Text(
        timeString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.customPlaceholder(visible = placeholder),
    )
}
