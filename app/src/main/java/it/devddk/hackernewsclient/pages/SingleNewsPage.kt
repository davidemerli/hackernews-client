package it.devddk.hackernewsclient.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel


@Composable
@ExperimentalPagerApi
@ExperimentalMaterial3Api
fun SingleNewsPage(navController: NavController, id: Int?) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val uiState = mViewModel.uiState.collectAsState(SingleNewsUiState.Loading)

    LaunchedEffect(id) {
        mViewModel.setId(id)
    }

    when (val uiStateValue = uiState.value) {
        is SingleNewsUiState.Error -> Error(throwable = uiStateValue.throwable)

        is SingleNewsUiState.ItemLoaded -> SingleNewsHeading(
            item = uiStateValue.item,
            onBackPressed = { navController.popBackStack() }
        )

        SingleNewsUiState.Loading -> Loading()
    }
}

@Composable
@ExperimentalPagerApi
@ExperimentalMaterial3Api
@SuppressLint("SetJavaScriptEnabled")
fun SingleNewsHeading(item: Item, onBackPressed: () -> Unit) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {},
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
        ) {
            item.text?.let { articleText ->
                Text(
                    articleText,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item.url?.let { url ->
                val state = rememberWebViewState(url)

                WebView(
                    state = state,
                    onCreated = { wv ->
                        wv.settings.javaScriptEnabled = true
                        wv.isScrollbarFadingEnabled = true
                        wv.isNestedScrollingEnabled = true
                    }
                )
            }
        }
    }
}

@Composable
fun Comments() {
    Text("Comments go here")
}


@Composable
fun Error(throwable: Throwable) {
    Text("Error")
}

@Composable
fun Loading() {
    Text("Loading")
}