package com.olirum.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "olirum_play_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LAST_TRACK_ID = stringPreferencesKey("last_track_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val LAST_SCAN_TIMESTAMP = longPreferencesKey("last_scan_timestamp")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val YOUTUBE_API_KEY = stringPreferencesKey("youtube_api_key")
    }

    val lastTrackId: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.LAST_TRACK_ID] }

    val lastPositionMs: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.LAST_POSITION_MS] ?: 0L }

    val shuffleEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.SHUFFLE_ENABLED] ?: false }

    val repeatMode: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.REPEAT_MODE] ?: 0 }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.DARK_MODE_ENABLED] ?: false }

    val youtubeApiKey: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.YOUTUBE_API_KEY] }

    suspend fun savePlaybackState(trackId: String, positionMs: Long) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.LAST_TRACK_ID] = trackId
                preferences[PreferencesKeys.LAST_POSITION_MS] = positionMs
            }
        } catch (_: Exception) {}
    }

    suspend fun saveShuffle(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SHUFFLE_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun saveRepeatMode(mode: Int) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.REPEAT_MODE] = mode
            }
        } catch (_: Exception) {}
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun saveLastScanTimestamp(timestamp: Long) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.LAST_SCAN_TIMESTAMP] = timestamp
            }
        } catch (_: Exception) {}
    }

    suspend fun saveYouTubeApiKey(key: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.YOUTUBE_API_KEY] = key.trim()
            }
        } catch (_: Exception) {}
    }
}
