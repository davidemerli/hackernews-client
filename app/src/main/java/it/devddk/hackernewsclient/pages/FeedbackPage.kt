package it.devddk.hackernewsclient.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import it.devddk.hackernewsclient.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.components.HomePageTopBar

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun FeedbackPage(navController: NavController, itemId: Int? = null) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState,
        query = "Feedback",
    ) {
        Scaffold(topBar = {
            HomePageTopBar(navController = navController, state = drawerState, query = "Feedback")
        }) {
            Column(
                modifier = Modifier
                    .padding(it.calculateTopPadding())
                    .fillMaxSize()
            ) {
                // form to send feedback
                Text(text = "Send feedback")

                itemId?.let { id ->
                    TextField(
                        value = id.toString(),
                        onValueChange = {},
                        leadingIcon = { Icon(Icons.Filled.Article, "Item ID") },
                        readOnly = true,
                    )
                }

                TextField(
                    value = "",
                    onValueChange = {},
                    singleLine = false,
                    placeholder = { Text("Your feedback") },
                    leadingIcon = { Icon(Icons.Filled.Feedback, "Feedback") },
                )

                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Send")
                }
            }
        }
    }
}
