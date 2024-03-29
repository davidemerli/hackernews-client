package it.devddk.hackernewsclient.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPrefs(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settingPrefs")

        // how much space is used for the indentation of the comments
        private val depthKey = floatPreferencesKey("comment_depth_size")
        // what does the user want to see first
        private val preferredViewKey = stringPreferencesKey("preferred_view")
        // which theme does the user want to use
        private val themeKey = stringPreferencesKey("theme")
        // whether the user wants to see the webview in dark mode
        private val darkModeKey = booleanPreferencesKey("darkMode")
        // font size when displaying comments in the app
        private val fontSizeKey = floatPreferencesKey("font_size")

        // webview settings
        private val javaScriptEnabledKey = booleanPreferencesKey("javaScriptEnabled")
        private val domStorageEnabledKey = booleanPreferencesKey("domStorageEnabled")
        private val allowFileAccessKey = booleanPreferencesKey("allowFileAccess")
        private val blockNetworkImageKey = booleanPreferencesKey("blockNetworkImage")
        private val allowContentAccessKey = booleanPreferencesKey("allowContentAccess")
        private val blockNetworkLoadsKey = booleanPreferencesKey("blockNetworkLoads")
        private val builtInZoomControlsKey = booleanPreferencesKey("builtInZoomControls")
        private val databaseEnabledKey = booleanPreferencesKey("databaseEnabled")
        private val displayZoomControlsKey = booleanPreferencesKey("displayZoomControls")
        private val javaScriptCanOpenWindowsAutomaticallyKey = booleanPreferencesKey("javaScriptCanOpenWindowsAutomatically")
        private val loadWithOverviewModeKey = booleanPreferencesKey("loadWithOverviewMode")
        private val loadsImagesAutomaticallyKey = booleanPreferencesKey("loadsImagesAutomatically")
        private val mediaPlaybackRequiresUserGestureKey = booleanPreferencesKey("mediaPlaybackRequiresUserGesture")
        private val offscreenPreRasterKey = booleanPreferencesKey("offscreenPreRaster")
        private val useWideViewPortKey = booleanPreferencesKey("useWideViewPort")
        private val geolocationEnabledKey = booleanPreferencesKey("geolocationEnabled")
        private val needInitialFocusKey = booleanPreferencesKey("needInitialFocus")
        private val supportMultipleWindowsKey = booleanPreferencesKey("supportMultipleWindows")
        private val supportZoomKey = booleanPreferencesKey("supportZoom")

        const val DEFAULT_DEPTH = 10f
        const val DEFAULT_FONT_SIZE = 16f
        const val DEFAULT_PREFERRED_VIEW = "article"
        const val DEFAULT_THEME = "system"
        const val DEFAULT_DARK_MODE = false

        val WEBVIEW_DEFAULTS = mapOf(
            "javaScriptEnabled" to true,
            "domStorageEnabled" to true,
            "allowFileAccess" to false,
            "blockNetworkImage" to false,
            "allowContentAccess" to false,
            "blockNetworkLoads" to false,
            "builtInZoomControls" to true,
            "databaseEnabled" to false,
            "displayZoomControls" to false,
            "javaScriptCanOpenWindowsAutomatically" to false,
            "loadWithOverviewMode" to true,
            "loadsImagesAutomatically" to true,
            "mediaPlaybackRequiresUserGesture" to false,
            "offscreenPreRaster" to false,
            "useWideViewPort" to true,
            "geolocationEnabled" to false,
            "needInitialFocus" to false,
            "supportMultipleWindows" to false,
            "supportZoom" to true,
        )
    }

    val depth: Flow<Float>
        get() = context.dataStore.data.map {
            it[depthKey] ?: DEFAULT_DEPTH
        }

    val preferredView: Flow<String>
        get() = context.dataStore.data.map {
            it[preferredViewKey] ?: DEFAULT_PREFERRED_VIEW
        }

    val theme: Flow<String>
        get() = context.dataStore.data.map {
            it[themeKey] ?: DEFAULT_THEME
        }

    val javaScriptEnabled: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[javaScriptEnabledKey] ?: WEBVIEW_DEFAULTS["javaScriptEnabled"]!!
        }

    val domStorageEnabled: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[domStorageEnabledKey] ?: WEBVIEW_DEFAULTS["domStorageEnabled"]!!
        }
    val allowFileAccess: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[allowFileAccessKey] ?: WEBVIEW_DEFAULTS["allowFileAccess"]!!
        }
    val blockNetworkImage: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[blockNetworkImageKey] ?: WEBVIEW_DEFAULTS["blockNetworkImage"]!!
        }
    val allowContentAccess: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[allowContentAccessKey] ?: WEBVIEW_DEFAULTS["allowContentAccess"]!!
        }
    val blockNetworkLoads: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[blockNetworkLoadsKey] ?: WEBVIEW_DEFAULTS["blockNetworkLoads"]!!
        }
    val builtInZoomControls: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[builtInZoomControlsKey] ?: WEBVIEW_DEFAULTS["builtInZoomControls"]!!
        }
    val databaseEnabled: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[databaseEnabledKey] ?: WEBVIEW_DEFAULTS["databaseEnabled"]!!
        }
    val displayZoomControls: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[displayZoomControlsKey] ?: WEBVIEW_DEFAULTS["displayZoomControls"]!!
        }
    val javaScriptCanOpenWindowsAutomatically: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[javaScriptCanOpenWindowsAutomaticallyKey] ?: WEBVIEW_DEFAULTS["javaScriptCanOpenWindowsAutomatically"]!!
        }
    val loadWithOverviewMode: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[loadWithOverviewModeKey] ?: WEBVIEW_DEFAULTS["loadWithOverviewMode"]!!
        }
    val loadsImagesAutomatically: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[loadsImagesAutomaticallyKey] ?: WEBVIEW_DEFAULTS["loadsImagesAutomatically"]!!
        }
    val mediaPlaybackRequiresUserGesture: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[mediaPlaybackRequiresUserGestureKey] ?: WEBVIEW_DEFAULTS["mediaPlaybackRequiresUserGesture"]!!
        }
    val offscreenPreRaster: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[offscreenPreRasterKey] ?: WEBVIEW_DEFAULTS["offscreenPreRaster"]!!
        }
    val useWideViewPort: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[useWideViewPortKey] ?: WEBVIEW_DEFAULTS["useWideViewPort"]!!
        }
    val geolocationEnabled: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[geolocationEnabledKey] ?: WEBVIEW_DEFAULTS["geolocationEnabled"]!!
        }
    val needInitialFocus: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[needInitialFocusKey] ?: WEBVIEW_DEFAULTS["needInitialFocus"]!!
        }
    val supportMultipleWindows: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[supportMultipleWindowsKey] ?: WEBVIEW_DEFAULTS["supportMultipleWindows"]!!
        }
    val supportZoom: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[supportZoomKey] ?: WEBVIEW_DEFAULTS["supportZoom"]!!
        }

    val darkMode: Flow<Boolean>
        get() = context.dataStore.data.map {
            it[darkModeKey] ?: DEFAULT_DARK_MODE
        }

    val fontSize: Flow<Float>
        get() = context.dataStore.data.map {
            it[fontSizeKey] ?: DEFAULT_FONT_SIZE
        }

    suspend fun setDarkMode(value: Boolean) {
        context.dataStore.edit { it[darkModeKey] = value }
    }

    val dataStore: DataStore<Preferences>
        get() = context.dataStore
}
