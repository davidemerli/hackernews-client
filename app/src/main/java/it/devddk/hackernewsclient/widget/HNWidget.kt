package it.devddk.hackernewsclient.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import it.devddk.hackernewsclient.domain.interaction.item.GetItemUseCase
import it.devddk.hackernewsclient.domain.interaction.item.GetNewStoriesUseCase
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExactAppWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()

        val test = prefs[ExactAppWidgetReceiver.test]

        LazyColumn(
            modifier = GlanceModifier.fillMaxSize().background(Color.Red)
        ) {
            item {
                Row(
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    Button(
                        text = "refresh", onClick = actionRunCallback<RefreshNewsCallback>()
                    )
                }
            }

            test?.let {
                items(test.toList()) { item ->
                    Text(text = item)
                }
            }
        }
    }
}

class ExactAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ExactAppWidget()

    private val coroutineScope = MainScope()

    @Inject
    private lateinit var getNewStories: GetNewStoriesUseCase

    @Inject
    private lateinit var getItemById: GetItemUseCase

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

        observeData(context)
    }

    private fun observeData(context: Context) {
        coroutineScope.launch {
//                getNewStories(TopStories).fold(
//                onSuccess = { it.map { id -> id.toString() }.toSet() },
//                onFailure = { setOf("error") }
//            )

            val glanceId = GlanceAppWidgetManager(context).getGlanceIds(ExactAppWidget::class.java)
                .firstOrNull()

            glanceId?.let {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                    pref.toMutablePreferences().apply {

                        this[test] = setOf("1 - ${Math.random()}", "2 - ${Math.random()}", "3 - ${Math.random()}")
                    }
                }

                glanceAppWidget.update(context, it)
            }
        }
    }

    companion object {
        val test = stringSetPreferencesKey("test")
    }
}

class RefreshNewsCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, ExactAppWidgetReceiver::class.java).apply {
            action = UPDATE_ACTION
        }

        context.sendBroadcast(intent)
    }

    companion object {
        const val UPDATE_ACTION = "updateAction"
    }
}
