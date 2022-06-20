package it.devddk.hackernewsclient.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.WEBVIEW_DEFAULTS
import timber.log.Timber

@Composable
@SuppressLint("SetJavaScriptEnabled")
fun ArticleView(
    modifier: Modifier = Modifier,
    webviewState: WebViewState,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (webviewState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        WebViewWithPrefs(
            webviewState = webviewState
        )
    }

}

@Composable
fun WebViewWithPrefs(
    modifier: Modifier = Modifier,
    webviewState: WebViewState,
) {
    val context = LocalContext.current
    val verticalScrollState = rememberScrollState()
    val dataStore = SettingPrefs(context)

    val javaScriptEnabled =
        dataStore.javaScriptEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["javaScriptEnabled"]!!)

    val domStorageEnabled =
        dataStore.domStorageEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["domStorageEnabled"]!!)

    val allowFileAccess =
        dataStore.allowFileAccess.collectAsState(initial = WEBVIEW_DEFAULTS["allowFileAccess"]!!)

    val blockNetworkImage =
        dataStore.blockNetworkImage.collectAsState(initial = WEBVIEW_DEFAULTS["blockNetworkImage"]!!)

    val allowContentAccess =
        dataStore.allowContentAccess.collectAsState(initial = WEBVIEW_DEFAULTS["allowContentAccess"]!!)

    val blockNetworkLoads =
        dataStore.blockNetworkLoads.collectAsState(initial = WEBVIEW_DEFAULTS["blockNetworkLoads"]!!)

    val builtInZoomControls =
        dataStore.builtInZoomControls.collectAsState(initial = WEBVIEW_DEFAULTS["builtInZoomControls"]!!)

    val databaseEnabled =
        dataStore.databaseEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["databaseEnabled"]!!)

    val displayZoomControls =
        dataStore.displayZoomControls.collectAsState(initial = WEBVIEW_DEFAULTS["displayZoomControls"]!!)

    val javaScriptCanOpenWindowsAutomatically =
        dataStore.javaScriptCanOpenWindowsAutomatically.collectAsState(initial = WEBVIEW_DEFAULTS["javaScriptCanOpenWindowsAutomatically"]!!)

    val loadWithOverviewMode =
        dataStore.loadWithOverviewMode.collectAsState(initial = WEBVIEW_DEFAULTS["loadWithOverviewMode"]!!)

    val loadsImagesAutomatically =
        dataStore.loadsImagesAutomatically.collectAsState(initial = WEBVIEW_DEFAULTS["loadsImagesAutomatically"]!!)

    val mediaPlaybackRequiresUserGesture =
        dataStore.mediaPlaybackRequiresUserGesture.collectAsState(initial = WEBVIEW_DEFAULTS["mediaPlaybackRequiresUserGesture"]!!)

    val offscreenPreRaster =
        dataStore.offscreenPreRaster.collectAsState(initial = WEBVIEW_DEFAULTS["offscreenPreRaster"]!!)

    val useWideViewPort =
        dataStore.useWideViewPort.collectAsState(initial = WEBVIEW_DEFAULTS["useWideViewPort"]!!)

    val geolocationEnabled =
        dataStore.geolocationEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["geolocationEnabled"]!!)

    val needInitialFocus =
        dataStore.needInitialFocus.collectAsState(initial = WEBVIEW_DEFAULTS["needInitialFocus"]!!)

    val supportMultipleWindows =
        dataStore.supportMultipleWindows.collectAsState(initial = WEBVIEW_DEFAULTS["supportMultipleWindows"]!!)

    val supportZoom =
        dataStore.supportZoom.collectAsState(initial = WEBVIEW_DEFAULTS["supportZoom"]!!)

    key(
        javaScriptEnabled.value,
        domStorageEnabled.value,
        allowFileAccess.value,
        blockNetworkImage.value,
        allowContentAccess.value,
        blockNetworkLoads.value,
        builtInZoomControls.value,
        databaseEnabled.value,
        displayZoomControls.value,
        javaScriptCanOpenWindowsAutomatically.value,
        loadWithOverviewMode.value,
        loadsImagesAutomatically.value,
        mediaPlaybackRequiresUserGesture.value,
        offscreenPreRaster.value,
        useWideViewPort.value,
        geolocationEnabled.value,
        needInitialFocus.value,
        supportMultipleWindows.value,
        supportZoom.value
    ) {
        WebView(
            state = webviewState,
            modifier = modifier.fillMaxHeight(),
            onCreated = { webview ->
                Timber.d("javascriptEnabled: ${javaScriptEnabled.value}")
                Timber.d("domStorageEnabled: ${domStorageEnabled.value}")
                Timber.d("allowFileAccess: ${allowFileAccess.value}")
                Timber.d("blockNetworkImage: ${blockNetworkImage.value}")
                Timber.d("allowContentAccess: ${allowContentAccess.value}")
                Timber.d("blockNetworkLoads: ${blockNetworkLoads.value}")
                Timber.d("builtInZoomControls: ${builtInZoomControls.value}")
                Timber.d("databaseEnabled: ${databaseEnabled.value}")
                Timber.d("displayZoomControls: ${displayZoomControls.value}")
                Timber.d("javaScriptCanOpenWindowsAutomatically: ${javaScriptCanOpenWindowsAutomatically.value}")
                Timber.d("loadWithOverviewMode: ${loadWithOverviewMode.value}")
                Timber.d("loadsImagesAutomatically: ${loadsImagesAutomatically.value}")
                Timber.d("mediaPlaybackRequiresUserGesture: ${mediaPlaybackRequiresUserGesture.value}")
                Timber.d("offscreenPreRaster: ${offscreenPreRaster.value}")
                Timber.d("useWideViewPort: ${useWideViewPort.value}")
                Timber.d("geolocationEnabled: ${geolocationEnabled.value}")
                Timber.d("needInitialFocus: ${needInitialFocus.value}")
                Timber.d("supportMultipleWindows: ${supportMultipleWindows.value}")
                Timber.d("supportZoom: ${supportZoom.value}")

                webview.settings.javaScriptEnabled = javaScriptEnabled.value
                webview.settings.domStorageEnabled = domStorageEnabled.value

                webview.isScrollContainer = supportZoom.value
                webview.isScrollbarFadingEnabled = true
                webview.isVerticalScrollBarEnabled = true
                webview.isHorizontalScrollBarEnabled = true

                webview.settings.setSupportZoom(supportZoom.value)
                webview.settings.loadWithOverviewMode = loadWithOverviewMode.value
                webview.settings.builtInZoomControls = builtInZoomControls.value

//            // false by default
                webview.settings.allowFileAccess = allowFileAccess.value
                webview.settings.blockNetworkImage = blockNetworkImage.value
                webview.settings.allowContentAccess = allowContentAccess.value
                webview.settings.blockNetworkLoads = blockNetworkLoads.value
                webview.settings.databaseEnabled = databaseEnabled.value
                webview.settings.displayZoomControls = displayZoomControls.value
                webview.settings.javaScriptCanOpenWindowsAutomatically =
                    javaScriptCanOpenWindowsAutomatically.value
                webview.settings.loadWithOverviewMode = loadWithOverviewMode.value
                webview.settings.loadsImagesAutomatically = loadsImagesAutomatically.value
                webview.settings.mediaPlaybackRequiresUserGesture =
                    mediaPlaybackRequiresUserGesture.value
//                webview.settings.offscreenPreRaster = offscreenPreRaster.value
                webview.settings.useWideViewPort = useWideViewPort.value
                webview.settings.setGeolocationEnabled(geolocationEnabled.value)
                webview.settings.setNeedInitialFocus(needInitialFocus.value)
                webview.settings.setSupportMultipleWindows(supportMultipleWindows.value)

                // TODO: configure those as settings
//            it.settings.cacheMode = true
//            it.settings.cursiveFontFamily = true
//            it.settings.defaultFixedFontSize = true
//            it.settings.defaultFontSize = true
//            it.settings.defaultTextEncodingName = true
//            it.settings.disabledActionModeMenuItems = true
//            it.settings.fantasyFontFamily = true
//            it.settings.fixedFontFamily = true
//            it.settings.forceDark = true
//            it.settings.layoutAlgorithm = true
//            it.settings.minimumFontSize = true
//            it.settings.minimumLogicalFontSize = true
//            it.settings.mixedContentMode = true
//            it.settings.safeBrowsingEnabled = true
//            it.settings.sansSerifFontFamily = true
//            it.settings.serifFontFamily = true
//            it.settings.standardFontFamily = true
//            it.settings.textZoom = true
//            it.settings.userAgentString = true
            }
        )
    }
}
