package com.example.unscramble.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
){

    private companion object {
        val LANGUAGE = stringPreferencesKey("language")
        val LEVEL_GAME = intPreferencesKey("level_game")
        const val TAG = "UserPreferencesRepo"
    }

    val userPrefs : Flow<UserPreferences> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
        val language = preferences[LANGUAGE] ?: "en"
        val levelGame = preferences[LEVEL_GAME] ?: LevelGame.EASY.level
        UserPreferences(language, levelGame)
    }

    suspend fun savePreferences(userPrefs : UserPreferences) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = userPrefs.language
            preferences[LEVEL_GAME] = userPrefs.levelGame
        }
    }

}
