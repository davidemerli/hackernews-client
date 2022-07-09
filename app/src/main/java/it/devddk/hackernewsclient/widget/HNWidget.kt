package it.devddk.hackernewsclient.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.IconImageProvider
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import androidx.glance.appwidget.lazy.LazyColumn
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.accompanist.pager.ExperimentalPagerApi
import it.devddk.hackernewsclient.MainActivity
import it.devddk.hackernewsclient.R
import it.devddk.hackernewsclient.activities.ArticleActivity
import it.devddk.hackernewsclient.activities.ItemIdKey
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import it.devddk.hackernewsclient.domain.model.collection.TopStories
import it.devddk.hackernewsclient.domain.model.utils.ItemId
import it.devddk.hackernewsclient.shared.components.news.getDomainName
import it.devddk.hackernewsclient.ui.theme.md_theme_dark_onBackground
import it.devddk.hackernewsclient.ui.theme.md_theme_dark_onSurface
import it.devddk.hackernewsclient.ui.theme.md_theme_dark_secondary
import it.devddk.hackernewsclient.ui.theme.md_theme_dark_tertiary
import it.devddk.hackernewsclient.utils.decodeJson
import it.devddk.hackernewsclient.utils.encodeJson
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ExactAppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    @Composable
    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun Content() {
        val context = LocalContext.current

        val prefs = currentState<Preferences>()
        val items = prefs[ExactAppWidgetReceiver.itemList]?.map {
            it.decodeJson(Result::class.java)
        }

        Timber.d("itemList: $items")

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_background))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = "HackerNews",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = ColorProvider(md_theme_dark_onSurface)
                    ),
                    modifier = GlanceModifier
                        .padding(top = 16.dp, start = 16.dp, bottom = 12.dp)
                        .clickable(onClick = actionStartActivity<MainActivity>())
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Image(
                        provider = IconImageProvider(Icon.createWithResource(context, R.drawable.ic_baseline_refresh_24)),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier.clickable(
                            onClick = actionRunCallback<RefreshNewsCallback>()
                        ).padding(end = 16.dp)
                    )
                }
            }

            LazyColumn(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(8.dp),
            ) {
                item {
                    Text(
                        "Top Stories",
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            color = ColorProvider(md_theme_dark_onSurface)
                        ),
                        modifier = GlanceModifier.fillMaxWidth().padding(8.dp)
                    )
                }

                items?.map { it ->
                    it.getOrNull()?.let { item ->

                        val itemMap = item as Map<*, *>
                        val itemId = (itemMap["id"] as Double).toInt()

                        item {
                            Column(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(ImageProvider(R.drawable.widget_background2))
                                    .clickable(
                                        onClick = actionStartActivity<ArticleActivity>(
                                            actionParametersOf(ItemIdKey to itemId)
                                        )
                                    )
                            ) {
                                Text(
                                    text = "${itemMap["by"]}" + itemMap["url"]?.let { " @ ${getDomainName(it.toString())}" },
                                    style = TextStyle(
                                        color = ColorProvider(color = md_theme_dark_secondary),
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                                    )
                                )

                                Text(
                                    text = itemMap["title"].toString(),
                                    style = TextStyle(
                                        color = ColorProvider(color = md_theme_dark_onBackground),
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                                    )
                                )

                                val points = itemMap["score"].toString().removeSuffix(".0")
                                val comments = (itemMap["descendants"] ?: "0").toString().removeSuffix(".0")

                                Text(
                                    text = "$points pt - $comments comments",
                                    style = TextStyle(
                                        color = ColorProvider(color = md_theme_dark_tertiary),
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize
                                    )
                                )
                            }
                        }

                        item {
                            Spacer(modifier = GlanceModifier.height(8.dp))
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

        Timber.d("Intent Action ${intent.action}")

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

                val items = it.subList(0, 10).map { item ->
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

    companion object {
        val itemList = stringSetPreferencesKey("itemList")
    }
}

class RefreshNewsCallback : ActionCallback {
    override suspend fun onRun(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val intent = Intent(context, ExactAppWidgetReceiver::class.java).apply {
            action = UPDATE_ACTION
        }

        Timber.d("RefreshNewsCallback")

        context.sendBroadcast(intent)
    }

    companion object {
        const val UPDATE_ACTION = "updateAction"
    }
}

val actionWidgetKey = ActionParameters.Key<ItemId>("action-widget-itemid")
