package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.navigation.NavController
import de.schnettler.datastore.compose.material3.PreferenceScreen
import de.schnettler.datastore.compose.material3.model.Preference
import de.schnettler.datastore.manager.PreferenceRequest
import it.devddk.hackernewsclient.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.components.HomePageTopBar
import it.devddk.hackernewsclient.utils.SettingPrefs

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun SettingsPage(navController: NavController) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context).dataStore

    val depthPreference = PreferenceRequest(
        key = floatPreferencesKey("comment_depth_size"),
        defaultValue = 6f,
    )

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
                    Preference.PreferenceItem.SeekBarPreference(
                        title = "Switch Preference",
                        summary = "A preference with a switch.",
                        singleLineTitle = true,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp)
                            )
                        },
                        request = depthPreference,
                        valueRange = 6.0f..24.0f,
                        valueRepresentation = { value ->
                            "${value.toInt()}".padStart(2, '0')
                        },
                    )
                ),
                dataStore = dataStore,
                statusBarPadding = true,
                modifier = Modifier.padding(top = it.calculateTopPadding())
            )
        }
    }
}
