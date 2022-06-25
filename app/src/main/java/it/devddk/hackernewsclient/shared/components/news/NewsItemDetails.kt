package it.devddk.hackernewsclient.shared.components.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.shared.components.customPlaceholder
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import timber.log.Timber
import java.time.LocalDateTime

@Composable
fun NewsItemDetails(
    modifier: Modifier = Modifier,
    url: String?,
    title: String?,
    author: String?,
    time: LocalDateTime?,
    placeholder: Boolean = false,
) {
    if (placeholder) {
        Column(
            modifier = modifier,
        ) {
            Text(
                text = url!!,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(4.dp)
                    .customPlaceholder(visible = true)
            )
            Text(
                text = title!!,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(4.dp)
                    .customPlaceholder(visible = true)
            )
            Text(
                text = author!!,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(4.dp)
                    .customPlaceholder(visible = true)
            )
        }
    } else {

        val context = LocalContext.current

        val domainString = remember { url?.let { getDomainName(it) } }

        val authorString = remember {
            when (author) {
                null -> context.getString(R.string.author_unknown)
                else -> context.getString(R.string.author, author)
            }
        }

        val titleString = remember {
            when (title) {
                null -> context.getString(R.string.title_unknown)
                else -> title
            }
        }

        val timeString = remember {
            when (time) {
                null -> context.getString(R.string.time_unknown)
                else -> TimeDisplayUtils(context).toDateTimeAgoInterval(time)
            }
        }

        val annotatedText = buildAnnotatedString {
            domainString?.let {
                withStyle(
                    MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline,
                    ).toSpanStyle()
                ) {
                    append(it)
                    append("\n")
                }
            }

            withStyle(
                MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                ).toSpanStyle()
            ) {
                append(titleString)
                append("\n")
            }

            withStyle(
                MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline,
                    baselineShift = BaselineShift(0.4f),
                ).toSpanStyle()
            ) {
                append(authorString)
            }

            withStyle(
                SpanStyle(
                    baselineShift = BaselineShift(0.4f),

                )
            ) {
                append(" - ")
            }

            withStyle(
                MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    baselineShift = BaselineShift(0.4f),
                ).toSpanStyle()
            ) {
                append(timeString)
                append("\n")
            }
        }

        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            onClick = { offset ->
                annotatedText.getStringAnnotations(
                    tag = "VIEW", start = offset,
                    end = offset
                )
                    .firstOrNull()?.let { annotation ->
                        Timber.d(annotation.item)
                    }
            },
            modifier = modifier.padding(bottom = 4.dp)
        )
    }
}
