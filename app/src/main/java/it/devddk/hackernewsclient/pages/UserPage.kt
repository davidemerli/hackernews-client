package it.devddk.hackernewsclient.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.search.SearchQuery
import it.devddk.hackernewsclient.domain.model.search.SearchTags
import it.devddk.hackernewsclient.pages.home.components.HNTopBar
import it.devddk.hackernewsclient.shared.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.shared.components.WebViewWithPrefs
import it.devddk.hackernewsclient.shared.components.customPlaceholder
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.viewmodels.SearchPageViewModel
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import it.devddk.hackernewsclient.viewmodels.UserUIState
import it.devddk.hackernewsclient.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun UserPage(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    username: String,
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)

    val coroutineScope = rememberCoroutineScope()

    val viewModel: SearchPageViewModel = viewModel()
    val itemViewModel: SingleNewsViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    val itemUiState by itemViewModel.uiState.collectAsState(initial = SingleNewsUiState.Loading)

    val selectedItem by derivedStateOf {
        if (itemUiState is SingleNewsUiState.ItemLoaded) {
            (itemUiState as SingleNewsUiState.ItemLoaded).item
        } else null
    }

    var expandedArticleView by rememberSaveable { mutableStateOf(false) }
    var readerMode by remember { mutableStateOf(false) }
    val darkMode by dataStore.darkMode.collectAsState(initial = SettingPrefs.DEFAULT_DARK_MODE)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val onItemClick = { item: Item ->
        coroutineScope.launch {
            itemViewModel.setId(item.id)
            readerMode = false
        }
    }

    val onItemClickComments = { item: Item ->
        coroutineScope.launch {
            itemViewModel.setId(item.id) // TODO: go to comments in tabbed
            readerMode = false
        }
    }

    val readabilityUrl = "https://readability.davidemerli.com?convert=${selectedItem?.url ?: ""}"

    val webViewState =
        rememberWebViewState(if (readerMode) readabilityUrl else selectedItem?.url ?: "")
    val webViewInstance by itemViewModel.webView.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        viewModel.updateAdvancedQuery(
            SearchQuery(
                tags = SearchTags.andOf(
                    SearchTags.NoPolls,
                    SearchTags.Author(username)
                )
            )
        )
        userViewModel.requestUser(username)
    }


    BackHandler(enabled = selectedItem != null, onBack = {
        coroutineScope.launch {
            itemViewModel.setId(null)
        }

        readerMode = false
        expandedArticleView = false
    })

    var openDialog by remember { mutableStateOf(false) }

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState
    ) {
        Scaffold(
            topBar = {
                HNTopBar(
                    title = "$username History",
                    leadingIcon = Icons.Filled.Badge,
                    navController = navController,
                    drawerState = drawerState,
                    selectedItem = selectedItem,
                    readerMode = readerMode,
                    darkMode = darkMode,
                    onClose = {
                        coroutineScope.launch {
                            itemViewModel.setId(null)
                        }
                    },
                    onDarkModeClick = {
                        coroutineScope.launch { dataStore.setDarkMode(!darkMode) }
                    },
                    onReaderModeClick = {
                        readerMode = !readerMode

                        if (readerMode) {
                            webViewInstance?.loadUrl(readabilityUrl)
                        } else {
                            webViewInstance?.loadUrl(selectedItem?.url ?: "")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (selectedItem != null) {
                    FloatingActionButton(onClick = {
                        openDialog = true
                    }) {
                        Icon(Icons.Filled.Fullscreen, "Expand")
                    }
                }
            }
        ) {
            when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Expanded -> {
                    SearchExpandedLayout(
                        modifier = Modifier.padding(top = it.calculateTopPadding()),
                        navController = navController,
                        onItemClick = { item -> onItemClick(item) },
                        onItemClickComments = { item -> onItemClickComments(item) },
                        selectedItem = selectedItem,
                        expanded = expandedArticleView,
                        onExpandedClick = { expandedArticleView = !expandedArticleView },
                        webViewState = webViewState,
                    ) {
                        UserDetails()
                    }
                }
                WindowWidthSizeClass.Compact,
                WindowWidthSizeClass.Medium,
                -> {
                    SearchCompactLayout(
                        modifier = Modifier.padding(top = it.calculateTopPadding()),
                        navController = navController,
                        selectedItem = selectedItem,
                        onItemClick = { item -> onItemClick(item) },
                        onItemClickComments = { item -> onItemClickComments(item) },
                        webViewState = webViewState,
                    ) {
                        UserDetails()
                    }
                }
            }

            if (openDialog) {
                Dialog(
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    onDismissRequest = { openDialog = false },
                ) {
                    WebViewWithPrefs(
                        modifier = Modifier.fillMaxSize(),
                        state = webViewState,
                        verticalScrollState = null
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun UserDetails() {

    val viewModel: UserViewModel = viewModel()

    val userState = viewModel.uiState.collectAsState()
    when (val theUserState = userState.value) {
        is UserUIState.Error -> {

        }
        is UserUIState.Loading -> {
            Text(
                text = "AAAAAAAAAAAAAAAAAAAAAAAAAA\n\n\n\n\n",
                style = MaterialTheme.typography.bodyLarge.copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        ),
                    ),
                ),
                maxLines = 5,
                modifier = Modifier.customPlaceholder(true),
            )
        }
        is UserUIState.UserLoaded -> {
            Text(
                text = theUserState.userData.about?.parseHTML() ?: "no_text",
                style = MaterialTheme.typography.bodyLarge.copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        ),
                    ),
                ),
                maxLines = 5,
                modifier = Modifier.customPlaceholder(false),
            )
        }
    }
}



