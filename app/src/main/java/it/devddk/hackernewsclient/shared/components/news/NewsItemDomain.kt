package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.shared.components.customPlaceholder

@Composable
fun NewsItemDomain(
    modifier: Modifier = Modifier,
    domain: String,
    placeholder: Boolean = false,
) {
    ClickableText(
        text = AnnotatedString(domain),
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.secondary,
//            textDecoration = TextDecoration.Underline, //TODO: add from domain section
        ),
        maxLines = 1,
        modifier = modifier
            .padding(bottom = 4.dp)
            .customPlaceholder(visible = placeholder),
        onClick = { /*TODO*/ },
    )
}
