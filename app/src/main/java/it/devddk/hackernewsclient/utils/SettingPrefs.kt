package it.devddk.hackernewsclient.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPrefs(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settingPrefs")
        private val depthKey = floatPreferencesKey("comment_depth_size")

        const val DEFAULT_DEPTH = 6f
    }

    val depth: Flow<Float>
        get() = context.dataStore.data.map {
            it[depthKey] ?: DEFAULT_DEPTH
        }

    suspend fun setDepth(value: Float) {
        context.dataStore.edit { it[depthKey] = value }
    }

    val dataStore: DataStore<Preferences>
        get() = context.dataStore
}
