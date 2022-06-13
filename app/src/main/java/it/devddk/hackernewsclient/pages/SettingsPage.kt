package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Wysiwyg
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import de.schnettler.datastore.compose.material3.PreferenceScreen
import de.schnettler.datastore.compose.material3.model.Preference
import de.schnettler.datastore.manager.PreferenceRequest
import it.devddk.hackernewsclient.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.components.HomePageTopBar
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.DEFAULT_DEPTH
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.DEFAULT_PREFERRED_VIEW
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.WEBVIEW_DEFAULTS
import java.util.*

fun depthPreference(): Preference.PreferenceItem.SeekBarPreference {
    val depthPreference = PreferenceRequest(
        key = floatPreferencesKey("comment_depth_size"),
        defaultValue = DEFAULT_DEPTH,
    )

    val min = DEFAULT_DEPTH
    val max = 24.0f

    return Preference.PreferenceItem.SeekBarPreference(
        title = "Comment Tab Size",
        summary = "Controls the tab size when viewing nested comments",
        singleLineTitle = true,
        icon = {
            Icon(
                imageVector = Icons.Filled.ClearAll,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .graphicsLayer { rotationY = 180f }

            )
        },
        request = depthPreference,
        valueRange = min..max,
        steps = (max - min).toInt(),
        valueRepresentation = { value ->
            "${value.toInt()}".padStart(2, '0')
        },
    )
}

fun preferredViewPreference(): Preference.PreferenceItem.ListPreference {
    val preferredViewPreference = PreferenceRequest(
        key = stringPreferencesKey("preferred_view"),
        defaultValue = DEFAULT_PREFERRED_VIEW,
    )

    return Preference.PreferenceItem.ListPreference(
        title = "Preferred View",
        summary = "Which view to open by default when opening a story",
        singleLineTitle = true,
        icon = {
            Icon(
                imageVector = Icons.Filled.Wysiwyg,
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
        },
        request = preferredViewPreference,
        entries = mapOf(
            "article" to "Article",
            "comments" to "Comments",
        ),
    )
}

fun webviewTogglePreference(
    setting: String,
    title: String? = null,
    summary: String,
): Preference.PreferenceItem.SwitchPreference {
    val preferenceRequest = PreferenceRequest(
        key = booleanPreferencesKey(setting),
        defaultValue = WEBVIEW_DEFAULTS[setting]!!,
    )

    return Preference.PreferenceItem.SwitchPreference(
        title = title ?: setting.camelToTitle(),
        summary = summary,
        singleLineTitle = false,
        request = preferenceRequest,
        icon = {}
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun SettingsPage(navController: NavController) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context).dataStore

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState,
        query = "Settings",
    ) {
        Scaffold(
            topBar = {
                HomePageTopBar(
                    navController = navController,
                    state = drawerState,
                    query = "Settings"
                )
            }
        ) {
            PreferenceScreen(
                items = listOf(
                    depthPreference(),
                    preferredViewPreference(),
                    Preference.PreferenceGroup(
                        title = "Webview",
                        preferenceItems = WEBVIEW_DEFAULTS.keys.map { pref ->
                            webviewTogglePreference(setting = pref, summary = "")
                        }
                    )
                ),
                dataStore = dataStore,
                statusBarPadding = true,
                modifier = Modifier.padding(top = it.calculateTopPadding())
            )
        }
    }
}

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

// String extensions
fun String.camelToTitle(): String {
    return camelRegex.replace(this) { " ${it.value}" }
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
