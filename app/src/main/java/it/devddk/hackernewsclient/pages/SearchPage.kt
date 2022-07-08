package it.devddk.hackernewsclient.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.search.SearchResult
import it.devddk.hackernewsclient.pages.home.components.HNTopBar
import it.devddk.hackernewsclient.shared.components.DepthIndicator
import it.devddk.hackernewsclient.shared.components.WebViewWithPrefs
import it.devddk.hackernewsclient.shared.components.news.NewsColorHint
import it.devddk.hackernewsclient.shared.components.news.NewsItem
import it.devddk.hackernewsclient.utils.SettingPrefs
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.SearchPageViewModel
import it.devddk.hackernewsclient.viewmodels.SearchResultUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun SearchPage(
    navController: NavController,
    query: String? = null,
    windowSizeClass: WindowSizeClass,
) {
    val context = LocalContext.current
    val dataStore = SettingPrefs(context)

    val coroutineScope = rememberCoroutineScope()

    val itemViewModel: SingleNewsViewModel = viewModel()

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

    var searchQuery by rememberSaveable { mutableStateOf(query) }
    val viewModel: SearchPageViewModel = viewModel()
    val lazyListState = rememberLazyListState()
    val resultList = viewModel.resultListFlow.collectAsState(initial = emptyList())

    if (searchQuery != null) {
        LaunchedEffect(searchQuery) {
            if (searchQuery!!.length >= 3) {
                viewModel.updateQuery(searchQuery!!)
            }
        }
    }

    BackHandler(enabled = selectedItem != null, onBack = {
        coroutineScope.launch {
            itemViewModel.setId(null)
        }

        readerMode = false
        expandedArticleView = false
    })

    var openDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HNTopBar(
                title = searchQuery ?: "Search",
                query = searchQuery,
                focusable = true,
                navController = navController,
                drawerState = drawerState,
                selectedItem = selectedItem,
                readerMode = readerMode,
                darkMode = darkMode,
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
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
                )
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
                )
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

@Composable
@OptIn(ExperimentalTextApi::class)
fun ResultItem(result: SearchResult, onClick: () -> Unit = {}) {
    Timber.d("ResultItem: ${result.item.by} - ${result.item.type}")

    val context = LocalContext.current

    when (result.item.type) {
        ItemType.STORY -> {
            NewsItem(item = result.item, onClick = onClick, placeholder = false)
        }
        ItemType.COMMENT -> {
            val timeString = remember(result.item) { TimeDisplayUtils(context).toDateTimeAgoInterval(result.item.time) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(8.dp)
            ) {
                NewsColorHint(color = MaterialTheme.colorScheme.tertiary)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.tertiary
                                ).toSpanStyle()
                            ) {
                                append("${result.item.by}")
                            }

                            withStyle(
                                MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ).toSpanStyle()
                            ) {
                                append(" on ")
                            }
                        }
                    )
                    Text(
                        text = "${result.searchMetadata.storyTitle}" + " ".repeat(30),
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )

                    Text(
                        text = "${result.item.text?.parseHTML() ?: "no_text"}\n\n\n\n\n",
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
                    )

                    Text(timeString, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        else -> {
            Text(result.item.id.toString())
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    resetQuery: () -> Unit,
    onBackClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    SmallTopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.background,
                    disabledIndicatorColor = MaterialTheme.colorScheme.background,
                    errorIndicatorColor = MaterialTheme.colorScheme.background,
                ),
                singleLine = true,
                // dismiss keyboard on submit
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = resetQuery) {
                Icon(Icons.Filled.Clear, contentDescription = "Clear")
            }
        }
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SearchExpandedLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    selectedItem: Item?,
    expanded: Boolean = false,
    onExpandedClick: () -> Unit,
    webViewState: WebViewState,
) {
    val viewModel: SingleNewsViewModel = viewModel()

    Row(
        modifier = modifier.fillMaxSize()
    ) {
        if (!expanded) {
            SearchCompactLayout(
                navController = navController,
                selectedItem = null,
                onItemClick = onItemClick,
                onItemClickComments = onItemClickComments,
                webViewState = webViewState,
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .fillMaxHeight()
            )
        }

        if (selectedItem != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(4.dp)
            ) {
                Icon(
                    if (expanded) Icons.Filled.ChevronRight else Icons.Filled.ChevronLeft,
                    contentDescription = if (!expanded) "Expand View" else "Collapse View",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.primary)
                        .size(28.dp)
                        .clickable { onExpandedClick() }
                )
            }

            TabbedView(
                navController = navController,
                item = selectedItem,
                webViewState = webViewState,
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_item_selected),
                    modifier = Modifier.alpha(0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SearchCompactLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    selectedItem: Item?,
    onItemClick: (Item) -> Unit,
    onItemClickComments: (Item) -> Unit,
    webViewState: WebViewState,
) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    val itemViewModel: SingleNewsViewModel = viewModel()
    val webViewInstance by itemViewModel.webView.collectAsState(initial = null)

    val viewModel: SearchPageViewModel = viewModel()
    val resultList = viewModel.resultListFlow.collectAsState(initial = emptyList())

    SwipeRefresh(
        state = swipeRefreshState,
        refreshTriggerDistance = 144.dp,
        onRefresh = {
            if (selectedItem != null) {
                webViewInstance?.reload()
            } else {
                // TODO:
            }
        }
    ) {
        if (selectedItem != null) {
            TabbedView(
                navController = navController,
                item = selectedItem,
                webViewState = webViewState,
                modifier = modifier
            )
        } else {
            LazyColumn(
                state = scrollState,
                modifier = modifier.fillMaxSize()
            ) {
                items(resultList.value.size) { index ->
                    LaunchedEffect(index.div(20)) {
                        viewModel.requestItem(index)
                    }

                    when (val result = resultList.value.getOrNull(index)) {
                        is SearchResultUiState.Loading -> {
                            Text("Loading More...")
                        }

                        is SearchResultUiState.ResultLoaded -> {
                            ResultItem(
                                result.result,
                                onClick = { onItemClick(result.result.item) }
                            )
                        }
                        null -> {
                        }
                    }
                }
            }
        }
    }
}
