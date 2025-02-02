package com.example.unscramble.unscramblerelease

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.unscramble.data.UserPreferences
import com.example.unscramble.data.UserPreferencesRepository
import com.example.unscramble.localdatabase.UnscrambleDatabase
import com.example.unscramble.repository.GamesRepository
import com.example.unscramble.repository.WordsRepository

// Datastore. Configuración básica de la app.
val Context.dataStore by preferencesDataStore(name = UserPreferences.SETTINGS_FILE)

class UnscrambleReleaseApplication : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var wordsRepository: WordsRepository
    lateinit var gamesRepository: GamesRepository

    // Contenedor de dependencias manuales que se usa por completo en la app
    override fun onCreate() {
        super.onCreate()

        // Creación de la instancia del repositorio de preferencias de usuario
        userPreferencesRepository = UserPreferencesRepository(dataStore)

        wordsRepository = WordsRepository(UnscrambleDatabase.getDatabase(this).wordsDAO())
        gamesRepository = GamesRepository(UnscrambleDatabase.getDatabase(this).gamesDAO())


    }

}