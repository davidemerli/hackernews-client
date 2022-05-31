package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.search.SearchResult
import it.devddk.hackernewsclient.viewmodels.SearchPageViewModel
import it.devddk.hackernewsclient.viewmodels.SearchResultUiState

@Composable
fun SearchPage(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val viewModel : SearchPageViewModel = viewModel()
    val lazyListState = rememberLazyListState()
    val resultList = viewModel.resultListFlow.collectAsState(initial = emptyList())


    LaunchedEffect(searchQuery) {
        if(searchQuery.length >= 3) {
            viewModel.updateQuery(searchQuery)
        }
    }

    LazyColumn(state = lazyListState) {
        item {
            TextField(value = searchQuery, onValueChange = {
                searchQuery = it
            })
        }
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
            val result = resultList.value.getOrNull(index)
            when(result) {
                is SearchResultUiState.Loading -> Text("Loading More...")
                is SearchResultUiState.ResultLoaded -> Text("${result.result.item.id}", modifier = Modifier.clickable {
                    navController.navigate("items/${result.result.item.id}")
                })
                null -> {

                }

            }

        }





    }
    
    
}