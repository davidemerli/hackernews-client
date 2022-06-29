package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.shared.components.topbars.FeedbackButton
import it.devddk.hackernewsclient.shared.components.topbars.OpenInBrowserButton
import it.devddk.hackernewsclient.shared.components.topbars.ShareButton
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HNTopBar(
    navController: NavController,
    drawerState: DrawerState,
    title: String = "Hacker News",
    leadingIcon: ImageVector? = null,
    selectedItem: Item? = null,
    onClose: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    var searchOpen by remember { mutableStateOf(false) }
    var searchString by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(searchOpen) {
        if (searchOpen) focusRequester.requestFocus()
    }

    SmallTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null && !searchOpen) {
                    Icon(
                        leadingIcon,
                        contentDescription = "Title Icon",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                TextField(
                    value = if (searchOpen) searchString else title,
                    enabled = searchOpen,
                    onValueChange = { searchString = it },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { navController.navigate("search/$searchString") }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                },
            ) {
                Icon(Icons.Filled.Menu, "Menu")
            }
        },
        actions = {
            IconButton(
                onClick = {
                    searchOpen = !searchOpen
                    focusManager.clearFocus()
                },
            ) {
                Icon(
                    if (!searchOpen) Icons.Filled.Search else Icons.Filled.Close,
                    contentDescription = if (!searchOpen) "Search" else "Close Search"
                )
            }

            if (selectedItem != null) {
                ShareButton(
                    itemUrl = selectedItem.url,
                    hnUrl = "https://news.ycombinator.com/item?id=${selectedItem.id}"
                )

                OpenInBrowserButton(
                    itemUrl = selectedItem.url,
                    hnUrl = "https://news.ycombinator.com/item?id=${selectedItem.id}"
                )

                FeedbackButton(navController, selectedItem.id)

                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.BookmarkAdd, "Save to read later")
                }

                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.StarOutline, "Save to favorites")
                }

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close Article",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}
