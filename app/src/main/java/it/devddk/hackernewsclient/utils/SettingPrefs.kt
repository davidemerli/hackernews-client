package it.devddk.hackernewsclient.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPrefs(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settingPrefs")

        // how much space is used for the indentation of the comments
        private val depthKey = floatPreferencesKey("comment_depth_size")
        // what does the user want to see first
        private val preferredViewKey = stringPreferencesKey("preferred_view")

        const val DEFAULT_DEPTH = 6f
        const val DEFAULT_PREFERRED_VIEW = "article"
    }

    val depth: Flow<Float>
        get() = context.dataStore.data.map {
            it[depthKey] ?: DEFAULT_DEPTH
        }

    val preferredView: Flow<String>
        get() = context.dataStore.data.map {
            it[preferredViewKey] ?: DEFAULT_PREFERRED_VIEW
        }

    suspend fun setDepth(value: Float) {
        context.dataStore.edit { it[depthKey] = value }
    }

    suspend fun setPreferredView(value: String) {
        context.dataStore.edit { it[preferredViewKey] = value }
    }

    val dataStore: DataStore<Preferences>
        get() = context.dataStore
}
