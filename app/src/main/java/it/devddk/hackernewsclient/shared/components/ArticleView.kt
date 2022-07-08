package it.devddk.hackernewsclient.shared.components

import android.annotation.SuppressLint
import android.os.Build
import android.view.MotionEvent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.DEFAULT_DARK_MODE
import it.devddk.hackernewsclient.utils.SettingPrefs.Companion.WEBVIEW_DEFAULTS
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch

@Composable
@SuppressLint("SetJavaScriptEnabled")
@Deprecated("Use WebViewWithPrefs and implement the progress elsewhere")
fun ArticleView(
    modifier: Modifier = Modifier,
    webviewState: WebViewState,
) {
    Column(
        modifier = modifier,
    ) {
        if (webviewState.isLoading) {
            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        WebViewWithPrefs(
            modifier = modifier,
            state = webviewState
        )
    }
}

@Composable
fun WebViewWithPrefs(
    modifier: Modifier = Modifier,
    state: WebViewState,
    setScroll: (Boolean) -> Unit = {},
    verticalScrollState: ScrollState? = rememberScrollState(),
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)

    val itemViewModel: SingleNewsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val javaScriptEnabled by dataStore.javaScriptEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["javaScriptEnabled"]!!)

    val domStorageEnabled by dataStore.domStorageEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["domStorageEnabled"]!!)

    val allowFileAccess by dataStore.allowFileAccess.collectAsState(initial = WEBVIEW_DEFAULTS["allowFileAccess"]!!)

    val blockNetworkImage by dataStore.blockNetworkImage.collectAsState(initial = WEBVIEW_DEFAULTS["blockNetworkImage"]!!)

    val allowContentAccess by dataStore.allowContentAccess.collectAsState(initial = WEBVIEW_DEFAULTS["allowContentAccess"]!!)

    val blockNetworkLoads by dataStore.blockNetworkLoads.collectAsState(initial = WEBVIEW_DEFAULTS["blockNetworkLoads"]!!)

    val builtInZoomControls by dataStore.builtInZoomControls.collectAsState(initial = WEBVIEW_DEFAULTS["builtInZoomControls"]!!)

    val databaseEnabled by dataStore.databaseEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["databaseEnabled"]!!)

    val displayZoomControls by dataStore.displayZoomControls.collectAsState(initial = WEBVIEW_DEFAULTS["displayZoomControls"]!!)

    val javaScriptCanOpenWindowsAutomatically by dataStore.javaScriptCanOpenWindowsAutomatically.collectAsState(initial = WEBVIEW_DEFAULTS["javaScriptCanOpenWindowsAutomatically"]!!)

    val loadWithOverviewMode by dataStore.loadWithOverviewMode.collectAsState(initial = WEBVIEW_DEFAULTS["loadWithOverviewMode"]!!)

    val loadsImagesAutomatically by dataStore.loadsImagesAutomatically.collectAsState(initial = WEBVIEW_DEFAULTS["loadsImagesAutomatically"]!!)

    val mediaPlaybackRequiresUserGesture by dataStore.mediaPlaybackRequiresUserGesture.collectAsState(initial = WEBVIEW_DEFAULTS["mediaPlaybackRequiresUserGesture"]!!)

    val offscreenPreRaster by dataStore.offscreenPreRaster.collectAsState(initial = WEBVIEW_DEFAULTS["offscreenPreRaster"]!!)

    val useWideViewPort by dataStore.useWideViewPort.collectAsState(initial = WEBVIEW_DEFAULTS["useWideViewPort"]!!)

    val geolocationEnabled by dataStore.geolocationEnabled.collectAsState(initial = WEBVIEW_DEFAULTS["geolocationEnabled"]!!)

    val needInitialFocus by dataStore.needInitialFocus.collectAsState(initial = WEBVIEW_DEFAULTS["needInitialFocus"]!!)

    val supportMultipleWindows by dataStore.supportMultipleWindows.collectAsState(initial = WEBVIEW_DEFAULTS["supportMultipleWindows"]!!)

    val supportZoom by dataStore.supportZoom.collectAsState(initial = WEBVIEW_DEFAULTS["supportZoom"]!!)

    val darkMode by dataStore.darkMode.collectAsState(initial = DEFAULT_DARK_MODE)

    key(
        javaScriptEnabled,
        domStorageEnabled,
        allowFileAccess,
        blockNetworkImage,
        allowContentAccess,
        blockNetworkLoads,
        builtInZoomControls,
        databaseEnabled,
        displayZoomControls,
        javaScriptCanOpenWindowsAutomatically,
        loadWithOverviewMode,
        loadsImagesAutomatically,
        mediaPlaybackRequiresUserGesture,
        offscreenPreRaster,
        useWideViewPort,
        geolocationEnabled,
        needInitialFocus,
        supportMultipleWindows,
        supportZoom,
        darkMode,
    ) {
        WebView(
            modifier = modifier,
            state = state,
            onCreated = { webview ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    webview.setOnTouchListener { view, event ->
                        if (event.action == MotionEvent.ACTION_UP) setScroll(true)

                        view.performClick()
                    }

                    webview.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                        if (scrollY - oldScrollY != 0) setScroll(false)
                    }
                }

                coroutineScope.launch { itemViewModel.setWebView(webview) }

                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) && darkMode) {
                    WebSettingsCompat.setForceDark(webview.settings, WebSettingsCompat.FORCE_DARK_ON)
                }

                webview.settings.javaScriptEnabled = javaScriptEnabled
                webview.settings.domStorageEnabled = domStorageEnabled

                webview.isScrollContainer = supportZoom
                webview.isScrollbarFadingEnabled = true
                webview.isVerticalScrollBarEnabled = true
                webview.isHorizontalScrollBarEnabled = true

                webview.settings.setSupportZoom(supportZoom)
                webview.settings.loadWithOverviewMode = loadWithOverviewMode
                webview.settings.builtInZoomControls = builtInZoomControls

                //            // false by default
                webview.settings.allowFileAccess = allowFileAccess
                webview.settings.blockNetworkImage = blockNetworkImage
                webview.settings.allowContentAccess = allowContentAccess
                webview.settings.blockNetworkLoads = blockNetworkLoads
                webview.settings.databaseEnabled = databaseEnabled
                webview.settings.displayZoomControls = displayZoomControls
                webview.settings.javaScriptCanOpenWindowsAutomatically = javaScriptCanOpenWindowsAutomatically
                webview.settings.loadWithOverviewMode = loadWithOverviewMode
                webview.settings.loadsImagesAutomatically = loadsImagesAutomatically
                webview.settings.mediaPlaybackRequiresUserGesture = mediaPlaybackRequiresUserGesture
//                                webview.settings.offscreenPreRaster = offscreenPreRaster
                webview.settings.useWideViewPort = useWideViewPort
                webview.settings.setGeolocationEnabled(geolocationEnabled)
                webview.settings.setNeedInitialFocus(needInitialFocus)
                webview.settings.setSupportMultipleWindows(supportMultipleWindows)

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
