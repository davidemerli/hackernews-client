package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.domain.model.items.StoryItem
import it.devddk.hackernewsclient.domain.model.utils.Expandable
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme
import it.devddk.hackernewsclient.utils.TimeDisplayUtils
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import org.koin.core.component.KoinComponent
import java.time.LocalDateTime

class MainActivity : ComponentActivity(), KoinComponent {

    lateinit var viewModel: HomePageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get()
        viewModel.requestArticles()

        setContent {
            HackerNewsClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    ListOfStuff()
                }
            }
        }
    }

    @Composable
    fun ArticleItem(article: Item) {
        Surface(
            tonalElevation = 16.dp
        ) {
            Text(
                text = article.title ?: "",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }

    @Composable
    fun NewsItem(
        item: Item,
    ) {
        val context = LocalContext.current
        val roundedShape = RoundedCornerShape(12.dp)
        val userString = remember(item) {
            val userId = item.by?.id
            if (userId != null) context.getString(R.string.author, userId)
            else getString(R.string.author_unknown)
        }
        val timeString = remember(item) {
            TimeDisplayUtils(context).toDateTimeAgoInterval(item.time)
        }
        val url = item.url
        val points = item.score
        val comments = item.descendants

        Box(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .clip(roundedShape)
            .background(Color.Red)
            .wrapContentHeight()) {

            Row {
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween

                ) {
                    Box(Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Green))
                    if (points != null) {
                        Text("$points pt", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Column(Modifier
                    .padding(4.dp)
                    .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween) {
                    Text(item.title ?: stringResource(R.string.title_unknown),
                        fontWeight = FontWeight.Bold)
                    if (url != null) {
                        Text(url, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$userString - $timeString",
                            style = MaterialTheme.typography.bodySmall)
                        if (comments != null) {
                            Row {
                                Icon(Icons.Rounded.CheckCircle,
                                    contentDescription = "Madonna",
                                    Modifier.size(20.dp))
                                Text("$comments", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ListOfStuff(list: List<Item>? = viewModel.articles.observeAsState().value) {
        val scrollState = rememberScrollState()

        Column(Modifier.verticalScroll(scrollState)) {
            list?.forEach {
                NewsItem(it)
                Spacer(Modifier.height(2.dp).fillMaxWidth())
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview(name: String = "peppe") {
        HackerNewsClientTheme {
            Column {
                // TODO: mock some data
                NewsItem(Item(
                    StoryItem(14,
                        false,
                        Expandable.compressed("giovanni"),
                        LocalDateTime.now(),
                        false,
                        emptyMap(),
                        "Super articol",
                        15,
                        12,
                        "www.com",
                        null)
                ))
            }
        }
    }

}
