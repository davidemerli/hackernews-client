package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.shared.components.customPlaceholder

@Composable
fun NewsItemPoints(
    modifier: Modifier = Modifier,
    score: Int,
    placeholder: Boolean = false
) {
    val context = LocalContext.current

    val pointsString = context.getString(R.string.points, score)

    Text(
        pointsString,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
        ),
        textAlign = TextAlign.Center,
        modifier = modifier.customPlaceholder(visible = placeholder)
    )
}
