package it.devddk.hackernewsclient.shared.components.topbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.utils.ItemId

@Composable
fun FeedbackButton(navController: NavController, itemId: ItemId) {
    IconButton(onClick = {
        navController.navigate("feedback/$itemId")
    }) {
        Icon(Icons.Filled.ReportProblem, "Report Problem")
    }
}
