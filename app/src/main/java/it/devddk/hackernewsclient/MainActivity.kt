package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import it.devddk.hackernewsclient.domain.model.items.Item
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import org.koin.core.component.KoinComponent

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
    fun ListOfStuff(list: List<Item>? = viewModel.articles.observeAsState().value) {
        Column {
            list?.forEach {
                ArticleItem(article = it)
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview(name: String = "peppe") {
        HackerNewsClientTheme {
            // TODO: mock some data
            ListOfStuff()
        }
    }

}
