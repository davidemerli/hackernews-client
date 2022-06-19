package it.devddk.hackernewsclient.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
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
        )
    }
}

@Composable
fun DepthIndicator(depth: Int) {
    val depthColors: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }

    Box(
        modifier = Modifier
            .width(10.dp)
            .fillMaxHeight()
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            .background(depthColors[depth % depthColors.size])
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun ExpandChildren(expanded: Boolean, numKids: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                pluralStringResource(R.plurals.comments, numKids, numKids),
                style = MaterialTheme.typography.bodySmall
            )

            Icon(
                if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                if (expanded) stringResource(R.string.close_comments) else stringResource(R.string.open_comments)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
// TODO: use ConstrainedLayout and add a "more" button with options such as "return to root"
fun CommentCard(
    item: Item,
    depth: Int = 0,
    depthSize: Int = 6,
    rootItem: Item,
    expanded: Boolean,
    onClick: () -> Unit = {},
    placeholder: Boolean = false,
) {
    val context = LocalContext.current
    val numKids = item.kids.size

    val paddingStart = (depth * depthSize).dp + 2.dp

    val depthColors: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }

    // obtains a background color for the comments which is a slight tint of the colorScheme secondary color
    val commentBackground = Color(
        ColorUtils.blendARGB(
            MaterialTheme.colorScheme.secondary.toArgb(),
            MaterialTheme.colorScheme.background.toArgb(),
            0.9f
        )
    )

    val timeString = remember(item) {
        TimeDisplayUtils(context).toDateTimeAgoInterval(item.time)
    }

    val isOriginalPoster = rootItem.by == item.by

    val byString = "${item.by}${if (isOriginalPoster) " (OP)" else ""}"

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(start = paddingStart, top = 4.dp, end = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(commentBackground)
    ) {
        DepthIndicator(depth = depth)

        Column(modifier = Modifier.fillMaxWidth()) {
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
                modifier = Modifier
                    .padding(4.dp)
                    .placeholder(
                        visible = placeholder,
                        color = PlaceholderDefaults.color(contentAlpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        highlight = PlaceholderHighlight.fade(),
                    ),
                style = MaterialTheme.typography.bodyLarge,
            )

//            CompositionLocalProvider(
//                LocalTextToolbar provides CustomTextToolbar(LocalView.current)
//            ) {
            if (item.text.isNullOrBlank()) {
                Text(
                    if (item.dead) "< removed comment >" else if (item.deleted) "< deleted comment >" else "< empty comment >",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Thin
                    ),
                    modifier = Modifier.padding(4.dp)
                )
            } else {
                SelectionContainer {
                    LinkifyText(
                        text = item.text!!.toSpanned(),
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Visible,
                        linkColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(
                                top = 4.dp,
                                start = 4.dp,
                                end = 4.dp,
                                bottom = if (numKids > 0) 4.dp else 16.dp
                            )
                            .placeholder(
                                visible = placeholder,
                                color = PlaceholderDefaults.color(contentAlpha = 0.8f),
                                shape = RoundedCornerShape(8.dp),
                                highlight = PlaceholderHighlight.fade(),
                            ),
                    )
                }
            }

            if (numKids > 0) {
                ExpandChildren(
                    expanded = expanded,
                    numKids = numKids,
                    onClick = onClick
                )
            }
        }
    }
}
