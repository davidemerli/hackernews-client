package it.devddk.hackernewsclient.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.collection.BestStories
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.pages.home.components.NewsColumn
import it.devddk.hackernewsclient.pages.home.components.NewsRow
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import it.devddk.hackernewsclient.viewmodels.ItemCollectionHolder
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomePage(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
) {
    val viewModel: HomePageViewModel = viewModel()

    val bestCollection = viewModel.collections[BestStories]!!
    val topCollection = viewModel.collections[TopStories]!!

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(text = "Hacker News")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Menu, "Menu")
                    }
                }
            )
        },
    ) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> {
                ExpandedLayout(
                    modifier = Modifier.padding(top = it.calculateTopPadding()),
                    navController = navController,
                    bestCollection = bestCollection,
                    topCollection = topCollection,
                    selectedItem = selectedItem,
                )
            }
            WindowWidthSizeClass.Compact,
            WindowWidthSizeClass.Medium -> {
                CompactLayout(
                    modifier = Modifier.padding(top = it.calculateTopPadding()),
                    navController = navController,
                    bestCollection = bestCollection,
                    topCollection = topCollection,
                    selectedItem = selectedItem,
                )
            }
        }
    }
}

@Composable
fun ExpandedLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    bestCollection: ItemCollectionHolder,
    topCollection: ItemCollectionHolder,
    selectedItem: Item?,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .fillMaxHeight()
                .background(Color.Blue)
        ) {
            item {
                NewsRow(
                    modifier = Modifier.fillMaxWidth(),
                    itemCollection = bestCollection,
                    onItemClick = { itemId -> { navController.navigate("items/$itemId") } },
                )
            }
        }
    }
}

@Composable
fun CompactLayout(
    modifier: Modifier = Modifier,
    navController: NavController,
    bestCollection: ItemCollectionHolder,
    topCollection: ItemCollectionHolder,
    selectedItem: Item?,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
        ) {
            Text("Best Stories")

            TextButton(onClick = { /*TODO*/ }) {
                Text("See more", modifier = Modifier.padding(end = 4.dp))
                Icon(Icons.Filled.ArrowForward, "Go to best stories")
            }
        }

        NewsRow(
            modifier = Modifier.fillMaxWidth(),
            itemCollection = bestCollection,
            onItemClick = { itemId -> { navController.navigate("items/$itemId") } },
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.1f)
                .padding(8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
        ) {
            Text("Top Stories")

            TextButton(onClick = { /*TODO*/ }) {
                Text("See more", modifier = Modifier.padding(end = 4.dp))
                Icon(Icons.Filled.ArrowForward, "Go to top stories")
            }
        }

        NewsColumn(
            modifier = Modifier.fillMaxWidth(),
            itemCollection = topCollection,
            onItemClick = { itemId -> { navController.navigate("items/$itemId") } },
        )
    }
}
