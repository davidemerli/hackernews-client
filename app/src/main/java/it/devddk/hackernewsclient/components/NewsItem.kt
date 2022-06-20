package it.devddk.hackernewsclient.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.ItemType
import it.devddk.hackernewsclient.domain.model.items.favorite
import it.devddk.hackernewsclient.domain.model.collection.UserDefinedItemCollection
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URI
import kotlin.math.roundToInt

fun getDomainName(url: String): String {
    return try {
        val uri = URI(url)
        val domain: String = uri.host

        domain.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}

// enum with subtypes
enum class ItemSubtype(val value: String) {
    ASK_HN("Ask HN"), SHOW_HN("Show HN"), TELL_HN("Tell HN")
}

fun Item.subtype(): ItemSubtype? {
    if (title != null) {
        if (title!!.startsWith("Ask HN")) {
            return ItemSubtype.ASK_HN
        } else if (title!!.startsWith("Show HN")) {
            return ItemSubtype.SHOW_HN
        } else if (title!!.startsWith("Tell HN")) {
            return ItemSubtype.TELL_HN
        }
    }

    return null
}

private val placeholderItem = Item(
    0,
    ItemType.STORY,
    title = "_".repeat(30),
    url = "https://news.ycombinator.com/",
    by = "_".repeat(10),
    time = null
)

@Composable
fun ColorHint(modifier: Modifier = Modifier, item: Item) {
    val palette: List<Color> = integerArrayResource(id = R.array.depth_colors).map { Color(it) }

    val backgroundColor = item.subtype()?.let {
        palette[it.ordinal]
    } ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .width(8.dp)
            .fillMaxHeight()
            .background(backgroundColor)
    )
}

@Composable
fun ItemDomain(modifier: Modifier = Modifier, item: Item, placeholder: Boolean = false) {
    item.url?.let { url ->
        val domain = getDomainName(url)

        ClickableText(
            text = AnnotatedString(domain),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
            ),
            maxLines = 1,
            modifier = modifier
                .padding(bottom = 4.dp)
                .placeholder(
                    visible = placeholder,
                    color = PlaceholderDefaults.color(contentAlpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    highlight = PlaceholderHighlight.fade(),
                ),
            onClick = { /*TODO*/ },
        )
    }
}

@Composable
fun ItemTitle(modifier: Modifier = Modifier, item: Item, placeholder: Boolean = false) {
    item.title?.let { title ->
        Text(
            title.trim(),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 19.5.sp
            ),
            modifier = modifier.placeholder(
                visible = placeholder,
                color = PlaceholderDefaults.color(contentAlpha = 0.8f),
                shape = RoundedCornerShape(8.dp),
                highlight = PlaceholderHighlight.fade(),
            ),
        )
    }
}

fun shareStringContent(context: Context, content: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(context, shareIntent, null)
}

@Composable
fun OptionsButton(modifier: Modifier = Modifier, item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val viewModel : HomePageViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

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
                modifier = Modifier.placeholder(
                    visible = placeholder,
                    color = PlaceholderDefaults.color(contentAlpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    highlight = PlaceholderHighlight.fade(),
                )
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            item.url?.let { url ->
                DropdownMenuItem(
                    text = { Text("Share Article") },
                    leadingIcon = {
                        Icon(Icons.Filled.Share, contentDescription = "Share Article")
                    },
                    onClick = {
                        shareStringContent(context, url)
                        expanded = false
                    },
                )
            }
            DropdownMenuItem(
                text = { Text("Share HN link") },
                leadingIcon = {
                    Icon(Icons.Filled.Share, contentDescription = "Share HN link")
                },
                onClick = {
                    shareStringContent(context, "https://news.ycombinator.com/item?id=${item.id}")
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(if(item.collections.favorite) "Remove from favorite" else "Add to favorite") },
                leadingIcon = {
                    Icon(Icons.Filled.Share, contentDescription = "Share HN link")
                },
                onClick = {
                    coroutineScope.launch {
                        if(!item.collections.favorite) {
                            viewModel.addToFavorite(item.id, UserDefinedItemCollection.Favorite)
                        } else {
                            viewModel.removeFromFavorite(item.id, UserDefinedItemCollection.Favorite)
                        }
                    }
                    expanded = false
                },
            )
        }
    }
}

@Composable
fun ItemBy(modifier: Modifier = Modifier, item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current

    val userString = remember(item) {
        when (item.by) {
            null -> context.getString(R.string.author_unknown)
            else -> context.getString(R.string.author, item.by)
        }
    }

    ClickableText(
        text = AnnotatedString(userString),
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.tertiary,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = modifier.placeholder(
            visible = placeholder,
            color = PlaceholderDefaults.color(contentAlpha = 0.8f),
            shape = RoundedCornerShape(8.dp),
            highlight = PlaceholderHighlight.fade(),
        ),
        onClick = { /*TODO*/ }
    )
}

@Composable
fun ItemPoints(modifier: Modifier = Modifier, item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current

    val pointsString = remember(item) {
        context.getString(R.string.points, item.score)
    }

    Text(
        pointsString,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
        ),
        textAlign = TextAlign.Center,
        modifier = modifier.placeholder(
            visible = placeholder,
            color = PlaceholderDefaults.color(contentAlpha = 0.8f),
            shape = RoundedCornerShape(8.dp),
            highlight = PlaceholderHighlight.fade(),
        ),
    )
}

@Composable
fun ItemTime(modifier: Modifier = Modifier, item: Item, placeholder: Boolean = false) {
    val context = LocalContext.current

    val timeString = remember(item) { TimeDisplayUtils(context).toDateTimeAgoInterval(item.time) }

    Text(
        timeString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.placeholder(
            visible = placeholder,
            color = PlaceholderDefaults.color(contentAlpha = 0.8f),
            shape = RoundedCornerShape(8.dp),
            highlight = PlaceholderHighlight.fade(),
        ),
    )
}

@Composable
fun ItemComments(
    modifier: Modifier = Modifier,
    item: Item,
    placeholder: Boolean = false,
    onClick: () -> Unit,
) {
    item.descendants?.let { comments ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.offset(x = 10.dp, y = 10.dp),
        ) {
            Text(
                "$comments",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .offset(x = 4.dp)
                    .placeholder(
                        visible = placeholder,
                        color = PlaceholderDefaults.color(contentAlpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        highlight = PlaceholderHighlight.fade(),
                    ),
            )
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.placeholder(
                        visible = placeholder,
                        color = PlaceholderDefaults.color(contentAlpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        highlight = PlaceholderHighlight.fade(),
                    )
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun NewsItem(
    item: Item = placeholderItem,
    placeholder: Boolean = false,
    onClick: () -> Unit = {},
    onClickComments: () -> Unit = {},
) {
    val swipeableState = rememberSwipeableState(
        0,
        animationSpec = SpringSpec(
            stiffness = Spring.StiffnessHigh
        ),
        confirmStateChange = {
            Timber.d(it.toString())
            false
        }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if ((swipeableState.progress.to - swipeableState.progress.from) * swipeableState.progress.fraction > 0.2f) Color.Yellow else MaterialTheme.colorScheme.surface)
    ) {

        val sizePx = with(LocalDensity.current) { -(maxWidth).toPx() }
        val anchors = mapOf(0f to 0, sizePx to 1, -sizePx to -1)

        ConstraintLayout(
            modifier = Modifier
                .height(128.dp)
                .fillMaxWidth()
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.2f) },
                    resistance = SwipeableDefaults.resistanceConfig(anchors.keys, 1f, 0.3f),
                    orientation = Orientation.Horizontal,
                )
                .clickable(onClick = onClick)
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            val (colorHint, domain, title, comments, more) = createRefs()

            val (pt, by, time) = createRefs()

            ColorHint(
                item = item,
                modifier = Modifier.constrainAs(colorHint) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
            )

            ItemDomain(
                item = item, placeholder = placeholder,
                modifier = Modifier.constrainAs(domain) {
                    top.linkTo(parent.top)
                    start.linkTo(colorHint.end, margin = 6.dp)
                }
            )

            ItemTitle(
                item = item, placeholder = placeholder,
                modifier = Modifier
                    .padding(end = 56.dp) // to avoid overlapping with the dropdown menu
                    .constrainAs(title) {
                        top.linkTo(domain.bottom)
                        start.linkTo(colorHint.end, margin = 6.dp)
                    }
            )

            ItemPoints(
                item = item,
                placeholder = placeholder,
                modifier = Modifier.constrainAs(pt) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(colorHint.end, margin = 6.dp)
                }
            )

            ItemBy(
                item = item,
                placeholder = placeholder,
                modifier = Modifier.constrainAs(by) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(pt.end, margin = 6.dp)
                }
            )

            ItemTime(
                item = item,
                placeholder = placeholder,
                modifier = Modifier.constrainAs(time) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(by.end, margin = 6.dp)
                }
            )

            // putting an animated placeholder also on those lags a bit much
            if (!placeholder) {
                OptionsButton(
                    item = item,
                    modifier = Modifier.constrainAs(comments) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
                )

                ItemComments(
                    item = item,
                    modifier = Modifier.constrainAs(more) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                    onClick = onClickComments
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Deprecated("Needs to be completely rewritten")
fun NewsItemTall(item: Item) {
    val context = LocalContext.current
    val roundedShape = RoundedCornerShape(16.dp)

    val userString = remember(item) {
        val userId = item.by
        if (userId != null) context.getString(R.string.author, userId)
        else context.getString(R.string.author_unknown)
    }

    val timeString = remember(item) {
        TimeDisplayUtils(context).toDateTimeAgoInterval(item.time)
    }

    val title = item.title?.replace("Ask HN:", "")?.trim()
    val url = item.url
    val points = item.score
    val comments = item.descendants

    val isAskHN = item.title?.contains("Ask HN") ?: false

    Card(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        shape = roundedShape
    ) {
        Row(modifier = Modifier.padding(start = 8.dp)) {
            Column {
                url?.let {
                    Text(
                        getDomainName(url),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)
                    )
                }
                if (isAskHN) {
                    Box(Modifier.padding(vertical = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                "Ask HN",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth(0.8f)
                        .height(if (isAskHN) 90.dp else 110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Column(Modifier.padding(start = 2.dp)) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "More",
                    )
                }
                IconButton(onClick = { /*TODO*/ }, Modifier.offset(y = -(4).dp)) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "More",
                )
            }
                IconButton(onClick = { /*TODO*/ }, Modifier.offset(y = -(8).dp)) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "More",
                )
            }
            }
        }
        Column(Modifier.padding(start = 10.dp)) {
            Text(
                title ?: "title_fail",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        "$userString - $timeString",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        "$points pt - 5 minutes read",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                "$comments",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.MailOutline,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
