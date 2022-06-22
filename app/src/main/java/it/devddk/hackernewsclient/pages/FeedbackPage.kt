package it.devddk.hackernewsclient.pages

import android.net.ConnectivityManager
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.feedback.Feedback
import it.devddk.hackernewsclient.shared.components.HNModalNavigatorPanel
import it.devddk.hackernewsclient.shared.components.HomePageTopBar
import it.devddk.hackernewsclient.shared.components.SegmentedButtons
import it.devddk.hackernewsclient.viewmodels.FeedbackViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun FeedbackPage(navController: NavController, itemId: Int? = null) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var feedbackMessage by remember { mutableStateOf("") }
    val uxValueState: MutableState<Int?> = remember { mutableStateOf(null) }
    val viewModel: FeedbackViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val submitFeedback: () -> Unit = {
        coroutineScope.launch {
            val feedback = Feedback(itemId, feedbackMessage, uxValueState.value ?: -1)

            val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
            val text = if (connectivityManager?.isDefaultNetworkActive == true) {
                "Thanks! Your feedback will be sent when you go back online."
            } else "Thanks for the feedback!"

            viewModel.postFeedback(feedback).fold(
                onSuccess = {
                    Toast.makeText(
                        context,
                        text,
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.popBackStack()
                },
                onFailure = {
                    Toast.makeText(
                        context,
                        "Failed to send feedback",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    HNModalNavigatorPanel(
        navController = navController,
        state = drawerState,
        query = "Feedback",
    ) {
        Scaffold(
            topBar = {
                HomePageTopBar(
                    navController = navController,
                    state = drawerState,
                    query = "Feedback"
                )
            },
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Submit") },
                    icon = { Icon(Icons.Filled.Send, "Send Feedback") },
                    onClick = submitFeedback,
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = it.calculateTopPadding(),
                        bottom = it.calculateBottomPadding(),
                        start = 24.dp,
                        end = 24.dp
                    )
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                itemId?.let { id ->
                    TextField(
                        value = id.toString(),
                        label = { Text("Item Id") },
                        onValueChange = {},
                        leadingIcon = { Icon(Icons.Filled.Article, "Item ID") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val placeholderText =
                    itemId?.let { "Share feedback about this item" } ?: "Share your thoughts"

                TextField(
                    feedbackMessage,
                    onValueChange = { value -> feedbackMessage = value },
                    placeholder = { Text(placeholderText) },
                    singleLine = false,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(vertical = 16.dp),
                )

                Text(
                    text = "How much does this affect the user experience?",
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SegmentedButtons(
                    state = uxValueState,
                    choices = listOf("1", "2", "3", "4", "5"),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}
