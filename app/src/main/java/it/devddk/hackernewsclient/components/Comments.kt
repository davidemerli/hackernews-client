package it.devddk.hackernewsclient.components

import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.pages.toSpanned
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.CommentUiState
import it.devddk.hackernewsclient.viewmodels.SingleNewsViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

fun Item.isExpandable() = kids.isNotEmpty()
fun CommentUiState.isExpandable() = this is CommentUiState.CommentLoaded && this.item.isExpandable()

private val placeholderItem = Item(
    0,
    ItemType.COMMENT,
    title = "_".repeat(30),
    url = "https://news.ycombinator.com/",
    by = "_".repeat(10),
    text = "_".repeat(100),
    time = null
)

@Composable
fun ExpandableComment(
    comment: CommentUiState,
    rootItem: Item,
    depthSize: Int,
    listState: LazyListState,
    navController: NavController,
) {
    val mViewModel: SingleNewsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var expanded by rememberSaveable { mutableStateOf(false) }

    val onClick = {
        if (comment.isExpandable()) {
            expanded = !expanded

            coroutineScope.launch {
                mViewModel.expandComment(comment.itemId, comment.isExpandable() && expanded)
            }
        }
    }

    when (comment) {
        is CommentUiState.CommentLoaded -> {
            CommentCard(
                comment.item,
                comment.depth,
                depthSize = depthSize,
                expanded = expanded,
                rootItem = rootItem,
                onClick = onClick,
                placeholder = false,
                listState = listState,
                navController = navController,
            )
        }
        is CommentUiState.Error -> Text(
            "< ERROR >",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
        // TODO: use placeholder CommentCard
        is CommentUiState.Loading -> CommentCard(
            placeholderItem,
            comment.depth,
            depthSize = depthSize,
            expanded = expanded,
            rootItem = rootItem,
            onClick = onClick,
            placeholder = true,
            listState = listState,
            navController = navController,
        )
    }
}

@Composable
fun CommentCard(
    item: Item,
    depth: Int = 0,
    depthSize: Int = 6,
    rootItem: Item,
    expanded: Boolean,
    listState: LazyListState,
    onClick: () -> Unit = {},
    placeholder: Boolean = false,
    navController: NavController,
) {
    val paddingStart = (depth * depthSize).dp + 2.dp

    // obtains a background color for the comments which is a slight tint of the colorScheme secondary color
    val commentBackground = Color(
        ColorUtils.blendARGB(
            MaterialTheme.colorScheme.secondary.toArgb(),
            MaterialTheme.colorScheme.background.toArgb(),
            0.9f
        )
    )

    val isOriginalPoster = rootItem.by == item.by

    ConstraintLayout(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .padding(paddingStart, 4.dp, 4.dp, 4.dp)
            .background(commentBackground)
    ) {
        val (depthIndicator, title, text, expand, more) = createRefs()

        DepthIndicator(
            depth = depth,
            modifier = Modifier.constrainAs(depthIndicator) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )

        CommentTitle(
            item = item,
            depth = depth,
            isOriginalPoster = isOriginalPoster,
            placeholder = placeholder,
            modifier = Modifier.constrainAs(title) {
                start.linkTo(depthIndicator.end, margin = 2.dp)
                top.linkTo(parent.top, margin = 1.dp)
            }
        )

        CommentText(
            item = item,
            placeholder = placeholder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
                .constrainAs(text) {
                    start.linkTo(depthIndicator.end, margin = 2.dp)
                    top.linkTo(title.bottom, margin = 2.dp)
                }
        )

        if (item.isExpandable()) {
            ExpandButton(
                expanded = expanded,
                onClick = onClick,
                item = item,
                modifier = Modifier.constrainAs(expand) {
                    top.linkTo(text.bottom, margin = 6.dp)
                    bottom.linkTo(parent.bottom, margin = 2.dp)
                    end.linkTo(parent.end, margin = 2.dp)
                }
            )
        }

        MoreOptions(
            item = item, placeholder = placeholder,
            listState = listState,
            rootItem = rootItem,
            navController = navController,
            modifier = Modifier.constrainAs(more) {
                top.linkTo(parent.top, margin = 2.dp)
                end.linkTo(parent.end, margin = 2.dp)
            }
        )
    }
}

@Composable
fun CommentTitle(
    modifier: Modifier = Modifier,
    item: Item,
    depth: Int,
    isOriginalPoster: Boolean,
    placeholder: Boolean = false,
) {
    val context = LocalContext.current

    val depthColors: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }
    val byString = "${item.by}${if (isOriginalPoster) " (OP)" else ""}"

    val timeString = remember(item) {
        TimeDisplayUtils(context).toDateTimeAgoInterval(item.time)
    }

    Text(
        text = buildAnnotatedString {
            pushStyle(
                MaterialTheme.typography.titleMedium.copy(
                    textDecoration = if (item.dead) TextDecoration.LineThrough else TextDecoration.Underline,
                    fontWeight = if (isOriginalPoster) FontWeight.Black else FontWeight.SemiBold,
                    color = if (isOriginalPoster) MaterialTheme.colorScheme.tertiary else depthColors[depth % depthColors.size].copy(
                        alpha = 1f
                    )
                ).toSpanStyle()
            )
            append(byString)
            pop()
            pushStyle(MaterialTheme.typography.bodySmall.toSpanStyle())
            append(" • ")
            append(timeString)
        },
        modifier = modifier
            .padding(4.dp)
            .placeholder(
                visible = placeholder,
                color = PlaceholderDefaults.color(contentAlpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                highlight = PlaceholderHighlight.fade(),
            ),
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
fun CommentText(modifier: Modifier = Modifier, item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current

    val linkColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    val highlightColor = MaterialTheme.colorScheme.tertiaryContainer

    if (item.text.isNullOrBlank()) {
        Text(
            if (item.dead) "< removed comment >" else if (item.deleted) "< deleted comment >" else "< empty comment >",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Thin
            ),
            modifier = Modifier.padding(4.dp)
        )
    } else {
        AndroidView(
            factory = { TextView(context) },
            update = {
                it.setLinkTextColor(linkColor.toArgb())
                it.setTextColor(textColor.toArgb())
                it.highlightColor = highlightColor.toArgb()

                it.text = item.text!!.toSpanned()
                it.setTextIsSelectable(true)
                Linkify.addLinks(it, Linkify.WEB_URLS)
            },
            modifier = modifier
                .padding(
                    top = 4.dp,
                    start = 4.dp,
                    end = 4.dp,
                    bottom = if (item.kids.isNotEmpty()) 4.dp else 16.dp
                )
                .placeholder(
                    visible = placeholder,
                    color = PlaceholderDefaults.color(contentAlpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    highlight = PlaceholderHighlight.fade(),
                ),
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun ExpandButton(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    item: Item,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .padding(4.dp),
    ) {
        Text(
            pluralStringResource(R.plurals.comments, item.kids.size, item.kids.size),
            style = MaterialTheme.typography.bodySmall
        )

        Icon(
            if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
            if (expanded) stringResource(R.string.close_comments) else stringResource(R.string.open_comments)
        )
    }
}

@Composable
fun DepthIndicator(modifier: Modifier = Modifier, depth: Int) {
    val depthColors: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }

    Box(
        modifier = modifier
            .width(10.dp)
            .fillMaxHeight()
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            .background(depthColors[depth % depthColors.size])
    )
}

@Composable
fun MoreOptions(
    modifier: Modifier = Modifier,
    item: Item,
    placeholder: Boolean = false,
    rootItem: Item,
    listState: LazyListState,
    navController: NavController,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val mViewModel: SingleNewsViewModel = viewModel()
    val comments = mViewModel.commentList.collectAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()

    if (placeholder) return

    Box(
        modifier = modifier
            .wrapContentSize(Alignment.TopStart)
            .offset(x = 10.dp, y = (-10).dp)
    ) {
        IconButton(
            onClick = { expanded = !expanded },
        ) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // TODO: move buttons in sub-composables
        // TODO: better scroll handling (if possible)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            item.parent?.let {
                comments.value.map { it.itemId }.indexOf(item.parent!!).let { index ->
                    if (index != -1) {
                        DropdownMenuItem(
                            text = { Text("Go To Parent") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.SubdirectoryArrowRight,
                                    contentDescription = "Scroll to parent",
                                    modifier = Modifier.rotate(180f)
                                )
                            },
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index, -2)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
            item.parent?.let {
                comments.value.map { it.itemId }.indexOf(item.parent!!).let { index ->
                    if (index != -1) {
                        DropdownMenuItem(
                            text = { Text("Go To Root") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.MoveUp,
                                    contentDescription = "Scroll to root comment"
                                )
                            },
                            onClick = {
                                coroutineScope.launch {
                                    var temp = comments.value.first { it.itemId == item.parent }

                                    while ((temp as CommentUiState.CommentLoaded).item.parent != rootItem.id) {
                                        temp =
                                            comments.value.first { it.itemId == (temp as CommentUiState.CommentLoaded).item.parent }
                                    }

                                    listState.animateScrollToItem(
                                        comments.value.map { it.itemId }.indexOf(temp.itemId),
                                        0,
                                    )

                                    Timber.d(
                                        "INDEX: ${
                                        comments.value.map { it.itemId }.indexOf(temp.itemId)
                                        }"
                                    )

                                    listState.animateScrollBy(-1f)

                                    expanded = false
                                }
                            }
                        )
                    }
                }
            }

            DropdownMenuItem(
                text = { Text("Share Comment") },
                leadingIcon = {
                    Icon(Icons.Filled.Share, contentDescription = "Share comment")
                },
                onClick = {
                    shareStringContent(context, "https://news.ycombinator.com/item?id=${item.id}")
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("Send Feedback") },
                leadingIcon = {
                    Icon(Icons.Filled.Feedback, contentDescription = "Send feedback")
                },
                onClick = {
                    navController.navigate("feedback/${item.id}")
                    expanded = false
                }
            )
        }
    }
}
