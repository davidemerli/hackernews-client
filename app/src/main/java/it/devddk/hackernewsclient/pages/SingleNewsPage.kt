package it.devddk.hackernewsclient.pages

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel


@Composable
fun SingleNewsPage(id : ItemId) {

    val viewModel : SingleNewsViewModel= viewModel()
}