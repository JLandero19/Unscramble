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
) {
    // Se ha creado para acceder de forma estatica a los valores
    private companion object {
        val LANGUAGE = stringPreferencesKey("language")
        val LEVEL_GAME = intPreferencesKey("level_game")
        const val TAG = "UserPreferencesRepo"
    }
    // dataStore.data.map -> recoge los datos y los mapea conviertiendolo en un objeto que en este caso es preferences
    val userPrefs : Flow<UserPreferences> = dataStore.data
        .catch { /* Captura de excepciones que puedan darse en el flujo */
            // Lo que se hace aquí es dividir las excepciones
            // Comprueba si es un error de Entrada/Salida
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                // Emite un fichero vacio de preferences
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
        // Coge el language de preferences, si está nulo por defecto en "en"
        val language = preferences[LANGUAGE] ?: "en"
        // Coge el levelGame de preferences, si está nulo por defecto en el nivel EASY [5]
        val levelGame = preferences[LEVEL_GAME] ?: LevelGame.EASY.level
        // Se encapsula en el objeto como UserPreferences que me he configurado en el flujo (Flow<UserPreferences>)
        UserPreferences(language, levelGame)
    }

    // userPrefs está casteada con nuestra data class UserPreferences
    // suspend -> este métodos solo se puede ejecutar en una corrutina
    suspend fun savePreferences(userPrefs: UserPreferences) {
        // Esto es para guardar la nueva configuración del usuario editando el actual
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = userPrefs.language
            preferences[LEVEL_GAME] = userPrefs.levelGame
        }
    }
}