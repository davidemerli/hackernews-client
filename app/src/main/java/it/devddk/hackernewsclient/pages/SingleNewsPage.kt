package it.devddk.hackernewsclient.pages

import android.text.Html
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.viewmodels.SingleNewsUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel


@Composable
fun SingleNewsPage(navController: NavController, id: Int?) {

    val mViewModel: SingleNewsViewModel = viewModel()
    val uiState = mViewModel.uiState.collectAsState(SingleNewsUiState.Loading)

    LaunchedEffect(id) {
        mViewModel.setId(id)
    }

    when (val uiStateValue = uiState.value) {
        is SingleNewsUiState.Error -> Error(throwable = uiStateValue.throwable)
        is SingleNewsUiState.ItemLoaded -> SingleNewsHeading(item = uiStateValue.item)
        SingleNewsUiState.Loading -> Loading()
    }
}

@Composable
fun SingleNewsHeading(item: Item) {
    Column {
        Text(
            "${item.title}",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 19.5.sp
            )
        )
        // Don't display it if it is null
        if (item.text != null) {
            Text(Html.fromHtml(
                "${item.text}",
                Html.FROM_HTML_OPTION_USE_CSS_COLORS
            ).toString())
        }

        Comments()
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