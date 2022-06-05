package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.components.NewsItem
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.viewmodels.SearchPageViewModel
import it.devddk.hackernewsclient.viewmodels.SearchResultUiState
import timber.log.Timber


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SearchPage(navController: NavController) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val viewModel: SearchPageViewModel = viewModel()
    val lazyListState = rememberLazyListState()
    val resultList = viewModel.resultListFlow.collectAsState(initial = emptyList())

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 3) {
            viewModel.updateQuery(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            SearchBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                resetQuery = {
                    searchQuery = ""
                    // TODO: empty list
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        },
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(top = it.calculateTopPadding()),
        ) {
            item {
                Text(if (searchQuery.length < 3) {
                    "Type at least 3 chars..."
                } else {
                    "Results for $searchQuery"
                })
            }

            items(resultList.value.size + 1) { index ->
                LaunchedEffect(index.div(20)) {
                    viewModel.requestItem(index)
                }

                when (val result = resultList.value.getOrNull(index)) {
                    is SearchResultUiState.ResultLoaded -> {
                        ResultItem(
                            result.result.item,
                            onClick = {
                                Timber.d("Clicked on ${result.result.item.id}")
                                navController.navigate("items/${result.result.item.id}")
                            }
                        )
                    }
                    else -> {
                        Text("Loading More...")
                    }
                }

            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ResultItem(item: Item, onClick: () -> Unit = {}) {
    when (item.type) {
        ItemType.STORY -> {
            NewsItem(item = item, onClick = onClick, placeholder = false)
        }
        else -> {
            Text(item.id.toString())
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

    SmallTopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.background,
                    disabledIndicatorColor = MaterialTheme.colorScheme.background,
                    errorIndicatorColor = MaterialTheme.colorScheme.background,
                ),
                singleLine = true,
                //dismiss keyboard on submit
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
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