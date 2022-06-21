package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.devddk.hackernewsclient.BuildConfig
import it.devddk.hackernewsclient.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.components.HomePageTopBar
import it.devddk.hackernewsclient.components.openInBrowser
import timber.log.Timber

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AboutPage(navController: NavController) {
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val scrollState = rememberScrollState()
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState,
        query = "About",
    ) {
        Scaffold(
            topBar = {
                HomePageTopBar(
                    navController = navController,
                    state = drawerState,
                    query = "About"
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = it.calculateTopPadding(),
                        bottom = it.calculateBottomPadding(),
                        start = 24.dp,
                        end = 24.dp
                    )
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                val annotatedText =
                    buildAnnotatedString {
                        val primary = MaterialTheme.colorScheme.primary

                        withStyle(MaterialTheme.typography.headlineMedium.toSpanStyle()) {
                            append("Hacker News Client\n")
                        }

                        withStyle(MaterialTheme.typography.titleMedium.toSpanStyle()) {

                            append("app version: $versionName ($versionCode)\n\n")
                        }

                        withStyle(MaterialTheme.typography.titleLarge.toSpanStyle()) {

                            append("Contacts:\n")
                        }

                        contactInfo(
                            name = "Davide Merli",
                            email = "davidemerli.dev@gmail.com",
                            github = "https://github.com/davidemerli"
                        )

                        append("\n")

                        contactInfo(
                            name = "Dario Passarello",
                            email = "dario.passarello@gmail.com",
                            github = "https://github.com/dario-passarello"
                        )

                        append("\n")

                        withStyle(
                            style = MaterialTheme.typography.titleLarge.toSpanStyle()
                        ) {
                            append("\nBuilt with:\n")
                        }

                        withStyle(
                            style = MaterialTheme.typography.titleMedium.toSpanStyle()
                        ) {
                            append("External Services:\n")
                        }


                        clickableUrl(
                            text = "HackerNews API on Firebase",
                            url = "https://github.com/HackerNews/API"
                        )
                        append(" - for general article acquisition\n")


                        clickableUrl(
                            text = "Algolia Hacker News API",
                            url = "https://hn.algolia.com/"
                        )
                        append(" - for targeted article and comments research\n")

                        withStyle(
                            style = MaterialTheme.typography.titleMedium.toSpanStyle()
                        ) {
                            append("\nLibraries:\n")
                        }

                        clickableUrl(
                            text = "Jetpack Compose\n",
                            url = "https://developer.android.com/jetpack/compose"
                        )
                        clickableUrl(
                            text = "Google Accompanist\n",
                            url = "https://github.com/google/accompanist"
                        )
                        clickableUrl(
                            text = "Compose Material 3 (You)\n",
                            url = "https://developer.android.com/jetpack/androidx/releases/compose-material3"
                        )

                        clickableUrl(
                            text = "Compose Preferences",
                            url = "https://github.com/Sh4dowSoul/ComposePreferences"
                        )

                        append("\n")

                        withStyle(
                            style = MaterialTheme.typography.titleMedium.toSpanStyle()
                        ) {
                            append("\nPrivacy:\n")
                        }

                        append("No data about use is collected. We only collect crash data with ")
                        clickableUrl(
                            text = "Firebase Crashlytics",
                            url = "https://firebase.google.com/products/crashlytics"
                        )
                        append(" to improve user experience. User feedback collected in the apposite app section is anonymous and stored on our Firebase Database.\n\n")


                        append("All news are sourced from ")

                        clickableUrl(
                            text = "https://news.ycombinator.com",
                            url = "https://news.ycombinator.com"
                        )

                    }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    onClick = { offset ->
                        // We check if there is an *URL* annotation attached to the text
                        // at the clicked position
                        annotatedText.getStringAnnotations(
                            tag = "URL", start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { annotation ->
                                Timber.d("annotation.item")
                                openInBrowser(context, annotation.item)
                            }
                    }
                )
            }
        }
    }
}

@Composable
fun AnnotatedString.Builder.contactInfo(name: String, email: String, github: String) {
    val primary = MaterialTheme.colorScheme.primary

    clickableUrl(text = name, url = github)

    withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
        append(" - ")
    }

    clickableUrl(text = email, url = "mailto:$email")
}

@Composable
fun AnnotatedString.Builder.clickableUrl(text: String, url: String) {
    val primary = MaterialTheme.colorScheme.primary

    withStyle(
        MaterialTheme.typography.bodyLarge.copy(
            color = primary,
            textDecoration = TextDecoration.Underline
        )
            .toSpanStyle()
    ) {
        pushStringAnnotation(tag = "URL", annotation = url)
        append(text)
    }
}
