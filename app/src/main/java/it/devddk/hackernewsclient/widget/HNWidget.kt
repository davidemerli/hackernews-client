package it.devddk.hackernewsclient.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.accompanist.pager.ExperimentalPagerApi
import it.devddk.hackernewsclient.MainActivity
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.components.getDomainName
import it.devddk.hackernewsclient.components.openInBrowser
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.ui.theme.md_theme_dark_onBackground
import it.devddk.hackernewsclient.ui.theme.md_theme_dark_onSurface
import it.devddk.hackernewsclient.utils.decodeJson
import it.devddk.hackernewsclient.utils.encodeJson
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class ExactAppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val items = prefs[ExactAppWidgetReceiver.itemList]?.map {
            it.decodeJson(Result::class.java)
        }

        Timber.d("itemList: $items")

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(R.drawable.widget_background))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "HackerNews",
                )

                Spacer(GlanceModifier.defaultWeight())

                Button(
                    text = "Refresh",
                    onClick = actionRunCallback<RefreshNewsCallback>()
                )
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_background))
                    .padding(8.dp),
            ) {
                items?.map { it ->
                    it.getOrNull()?.let { item ->

                        val itemMap = item as Map<*, *>

                        Column(
                            modifier = GlanceModifier
                                .padding(horizontal = 4.dp, vertical=8.dp)
                                .clickable(onClick = actionStartActivity<MainActivity>())
                        ) {
                            Text(
                                text = "${itemMap["by"]} - ${getDomainName(itemMap["url"].toString())}",
                                style = TextStyle(
                                    color = ColorProvider(color = md_theme_dark_onBackground),
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )
                            )

                            Text(
                                text = itemMap["title"].toString(),
                                style = TextStyle(
                                    color = ColorProvider(color = md_theme_dark_onBackground),
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class ExactAppWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    override val glanceAppWidget = ExactAppWidget()

    private val coroutineScope = MainScope()

    private val getNewStoriesUseCase: GetNewStoriesUseCase by inject()
    private val getItemUseCase: GetItemUseCase by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        observeData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            RefreshNewsCallback.UPDATE_ACTION -> observeData(context)
        }
    }

    private fun observeData(context: Context) {
        coroutineScope.launch {

            getNewStoriesUseCase(TopStories).getOrNull()?.let {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIds(ExactAppWidget::class.java)
                    .firstOrNull()

                Timber.d("items $it")

                val items = it.subList(0, 5).map { item ->
                    Timber.d("item $item - ${getItemUseCase(item).encodeJson()}")

                    getItemUseCase(item).encodeJson()
                }.toSet()

                glanceId?.let { id ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { pref ->

                        pref.toMutablePreferences().apply {
                            this[itemList] = items

                            glanceAppWidget.updateAll(context)
                        }
                    }
                }
            }
        }
    }

    private fun openArticle(context: Context) {

    }

    companion object {
        val itemList = stringSetPreferencesKey("itemList")
    }
}

class RefreshNewsCallback : ActionCallback {
    override suspend fun onRun(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, ExactAppWidgetReceiver::class.java).apply {
            action = UPDATE_ACTION
        }

        context.sendBroadcast(intent)
    }

    companion object {
        const val UPDATE_ACTION = "updateAction"
    }
}

class OpenArticleCallback : ActionCallback {
    override suspend fun onRun(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        openInBrowser(context, "https://news.ycombinator.com/item?id=${parameters[actionWidgetKey]}")

        // start actiity

    }

}

val actionWidgetKey = ActionParameters.Key<ItemId>("action-widget-itemid")
