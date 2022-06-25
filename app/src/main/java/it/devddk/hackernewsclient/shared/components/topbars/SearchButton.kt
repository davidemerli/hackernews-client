package it.devddk.hackernewsclient.shared.components.topbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SearchButton(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    IconButton(
        onClick = {
            navController.navigate("search")
        },
        modifier = modifier
    ) {
        Icon(Icons.Rounded.Search, "Search")
    }
}
