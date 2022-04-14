package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import it.devddk.hackernewsclient.domain.model.utils.NewStories
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme
import it.devddk.hackernewsclient.viewmodels.HomePageViewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    lateinit var viewModel : HomePageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get()
        viewModel.requestArticles()

        setContent {
            HackerNewsClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ListOfStuff()
                }
            }
        }
    }

    @Composable
    fun ListOfStuff() {

        val list  = viewModel.articles.observeAsState().value

        Column {
            list?.map { x -> Text(x.title ?: "AAAA") }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview(name: String = "peppe") {
        HackerNewsClientTheme {
            Column {
                Text("Titolo 1")
                Text("Titolo 2")
            }
        }
    }

    @Composable
    fun ArticleView() {
        Column() {

        }
    }
}
