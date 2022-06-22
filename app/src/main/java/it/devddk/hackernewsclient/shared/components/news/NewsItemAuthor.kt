package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.shared.components.customPlaceholder

@Composable
fun NewsItemAuthor(
    modifier: Modifier = Modifier,
    author: String?,
    placeholder: Boolean = false
) {
    val context = LocalContext.current

    val userString = remember {
        when (author) {
            null -> context.getString(R.string.author_unknown)
            else -> context.getString(R.string.author, author)
        }
    }

    ClickableText(
        text = AnnotatedString(userString),
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.tertiary,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = modifier.customPlaceholder(visible = placeholder),
        onClick = { /*TODO*/ }
    )
}
