package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.shared.components.news.NewsItem
import it.devddk.hackernewsclient.utils.encodeJson
import it.devddk.hackernewsclient.utils.urlEncode
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
                Text(
                    if (searchQuery.length < 3) {
                        "Type at least 3 chars to start searching"
                    } else {
                        "Results for $searchQuery"
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }

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
                            result.result.item,
                            onClick = {
                                Timber.d("Clicked on ${result.result.item.id}")
                                navController.navigate(
                                    "items/${result.result.item.id}"
                                )
                            }
                        )
                    }
                    null -> {
                    }
                }
            }
    }
}

@Composable
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

            LaunchedEffect(searchQuery) {
                focusRequester.requestFocus()
            }
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
