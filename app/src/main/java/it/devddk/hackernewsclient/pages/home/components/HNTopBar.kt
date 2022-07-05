package it.devddk.hackernewsclient.pages.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.webkit.WebViewFeature
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.shared.components.topbars.OpenInBrowserButton
import it.devddk.hackernewsclient.shared.components.topbars.ShareButton
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HNTopBar(
    navController: NavController,
    drawerState: DrawerState,
    title: String = "Hacker News",
    query: String? = "",
    focusable: Boolean = false,
    leadingIcon: ImageVector? = null,
    selectedItem: Item? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    readerMode: Boolean = false,
    darkMode: Boolean = false,
    onClose: () -> Unit = {},
    onReaderModeClick: () -> Unit = {},
    onDarkModeClick: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    var searchOpen by remember { mutableStateOf(false) }
    var searchString by remember { mutableStateOf(query ?:"") }

    val focusManager = LocalFocusManager.current

    @Composable
    fun buttonBackground(value: Boolean): Color {
        return if (value) MaterialTheme.colorScheme.secondary else Color.Transparent
    }

    @Composable
    fun buttonForeground(value: Boolean): Color {
        return if (value) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    }

    SmallTopAppBar(
        title = {
            TopBarTitle(
                title = title,
                leadingIcon = leadingIcon,
                selectedItem = selectedItem,
                onValueChange = { searchString = it },
                onSearch = { navController.navigate("search/$searchString") },
                searchOpen = searchOpen or focusable,
                searchString = searchString,
            )
        },
        navigationIcon = navigationIcon ?: {
            if (selectedItem == null) {
                IconButton(
                    onClick = { coroutineScope.launch { drawerState.open() } },
                ) {
                    Icon(Icons.Filled.Menu, "Menu")
                }
            } else {
                IconButton(
                    onClick = onClose,
                ) {
                    Icon(Icons.Filled.ArrowBack, "Back")
                }
            }
        },
        actions = {
            if (selectedItem == null) {
                SearchButton(searchOpen, onClick = {
                    searchOpen = !searchOpen
                    focusManager.clearFocus()
                })
            } else {
                ShareButton(
                    itemUrl = selectedItem.url,
                    hnUrl = "https://news.ycombinator.com/item?id=${selectedItem.id}"
                )

                OpenInBrowserButton(
                    itemUrl = selectedItem.url,
                    hnUrl = "https://news.ycombinator.com/item?id=${selectedItem.id}"
                )

                IconButton(
                    onClick = onReaderModeClick,
                ) {
                    Icon(
                        Icons.Filled.ChromeReaderMode,
                        contentDescription = "Reader Mode",
                        tint = buttonForeground(readerMode),
                        modifier = Modifier.background(buttonBackground(readerMode), CircleShape).padding(6.dp)
                    )
                }

                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    IconButton(
                        onClick = onDarkModeClick,
                    ) {
                        Icon(
                            Icons.Filled.DarkMode,
                            contentDescription = "Dark Mode",
                            tint = buttonForeground(darkMode),
                            modifier = Modifier.background(buttonBackground(darkMode), CircleShape).padding(6.dp)
                        )
                    }
                }

                MoreButton(
                    navController = navController,
                    item = selectedItem,
                )
            }
        }
    )
}

@Composable
fun MoreButton(
    navController: NavController,
    item: Item,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, "More")

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Send Feedback") },
                onClick = { navController.navigate("feedback/${item.id}") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.ReportProblem,
                        "Report Problem"
                    )
                }
            )

            DropdownMenuItem(
                text = { Text("Save to read later") },
                onClick = { /*TODO*/ },
                leadingIcon = {
                    Icon(Icons.Filled.BookmarkAdd, "Save to read later")
                },
            )

            DropdownMenuItem(
                text = { Text("Save to favorites") },
                onClick = { /*TODO*/ },
                leadingIcon = {
                    Icon(Icons.Filled.StarOutline, "Save to favorites")
                },
            )
        }
    }
}

@Composable
fun SearchButton(
    searchOpen: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            if (!searchOpen) Icons.Filled.Search else Icons.Filled.Close,
            contentDescription = if (!searchOpen) "Search" else "Close Search"
        )
    }
}

@Composable
fun TopBarTitle(
    title: String = "Hacker News",
    leadingIcon: ImageVector? = null,
    selectedItem: Item?,
    searchOpen: Boolean,
    searchString: String,
    onValueChange: (String) -> Unit,
    onSearch: KeyboardActionScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchOpen) {
        if (searchOpen) focusRequester.requestFocus()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedItem == null) {
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
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(onSearch = onSearch),
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
    }
}
