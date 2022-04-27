package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import it.devddk.hackernewsclient.components.NewsItem
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.StoryItem
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.time.LocalDateTime

class MainActivity : ComponentActivity(), KoinComponent {

    lateinit var viewModel: HomePageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get()
        viewModel.requestArticles()

        setContent {
            HackerNewsClientTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Test2()
                }
            }
        }
    }

    @Composable
    fun ArticleItem(article: Item) {
        Surface(
            tonalElevation = 16.dp
        ) {
            Text(
                text = article.title ?: "",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun Test2() {
        val scrollState = rememberScrollState()
        val list = viewModel.articles.observeAsState().value


        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = { Icon(Icons.Rounded.Menu, "Menu") },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Rounded.Search, "Search")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Rounded.AccountCircle, "Notifications")
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.verticalScroll(scrollState)) {
                list?.forEach {
                    NewsItem(it)
                }
            }
        }
    }

    @Composable
    fun ListOfStuff(list: List<Item>? = viewModel.articles.observeAsState().value) {
        val scrollState = rememberScrollState()

        Column(Modifier.verticalScroll(scrollState)) {
            list?.forEach {
                NewsItem(it)
                Spacer(Modifier
                    .height(2.dp)
                    .fillMaxWidth())
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showBackground = true, widthDp = 441, heightDp = 980)
    @Composable
    fun DefaultPreview2() {
        Test()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Test() {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        // icons to mimic drawer destinations
        val items = listOf(Icons.Default.Favorite, Icons.Default.Face, Icons.Default.Email)
        val selectedItem = remember { mutableStateOf(items[0]) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item, contentDescription = null) },
                        label = { Text(item.name) },
                        selected = item == selectedItem.value,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem.value = item
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = if (drawerState.isClosed) ">>> Swipe >>>" else "<<< Swipe <<<")
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { scope.launch { drawerState.open() } }) {
                        Text("Click to open")
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showBackground = true, widthDp = 441, heightDp = 980)
    @Composable
    fun DefaultPreview(name: String = "peppe") {
        val scrollState = rememberScrollState()

        HackerNewsClientTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            navigationIcon = { Icon(Icons.Rounded.Menu, "Menu") },
                            actions = {
                                IconButton(onClick = { }) {
                                    Icon(Icons.Rounded.Search, "Search")
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Rounded.AccountCircle, "Notifications")
                                }
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(4.dp),
                    ) {
                        NewsItem(Item(
                            StoryItem(14,
                                false,
                                Expandable.compressed("giovanni"),
                                LocalDateTime.now(),
                                false,
                                emptyMap(),
                                "Super articol",
                                15,
                                12,
                                "www.com",
                                null)
                        ))
                        Spacer(modifier = Modifier.height(2.dp))
                        NewsItem(Item(
                            StoryItem(14,
                                false,
                                Expandable.compressed("giovanni"),
                                LocalDateTime.now(),
                                false,
                                emptyMap(),
                                "Super articol",
                                15,
                                12,
                                "www.com",
                                null)
                        ))
                    }
                }
            }
        }
    }

}
