package it.devddk.hackernewsclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.utils.NewStories
import it.devddk.hackernewsclient.ui.theme.HackerNewsClientTheme
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.lang.Exception

class MainActivity : ComponentActivity(), KoinComponent {

    val getNewStories : GetNewStoriesUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            val c = getNewStories(NewStories).getOrThrow()
            Timber.e("Ciaooo")
            if(c.isEmpty()) {
                Timber.e("Empty")
            }
            c.forEach {
                Timber.i("bee ${it.title}")
            }
        }

        setContent {
            HackerNewsClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview(name: String = "peppe") {
        HackerNewsClientTheme {
            Greeting("Android")
        }
    }

    @Composable
    fun ArticleView() {
        Column() {

        }
    }
}
