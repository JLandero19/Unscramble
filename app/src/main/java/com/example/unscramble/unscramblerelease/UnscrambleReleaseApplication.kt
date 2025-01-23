package com.example.unscramble.unscramblerelease

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.unscramble.data.UserPreferences
import com.example.unscramble.data.UserPreferencesRepository

// Datastore. Configuración básica de la app.
// Se está enlazando el fichero de configuración que se llamará "settings"
// "settings" se encuentra en el UserPreferences.kt
val Context.dataStore by preferencesDataStore(name = UserPreferences.SETTINGS_FILE)

class UnscrambleReleaseApplication: Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    // Contenedor de dependencias manuales que se usa por completo en la app
    override fun onCreate() {
        super.onCreate()

        // Instancias Singleton
        // Crea una instancia única del repositorio
        // El repositorio se enlaza a la capa de ViewModel
        userPreferencesRepository = UserPreferencesRepository(this.dataStore)

        // by lazy -> evita consumir recursos si no se está utilzando
        // no funciona con configuración private set de lateinit var userPreferencesRepository: UserPreferencesRepository
//        val userPreferencesRepository : UserPreferencesRepository by lazy {
//            UserPreferencesRepository(this.dataStore)
//        }
    }
}