package com.example.unscramble.ui.game

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.unscramble.data.Language
import com.example.unscramble.data.LevelGame
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.UserPreferences
import com.example.unscramble.data.UserPreferencesRepository
import com.example.unscramble.data.allSpanishWords
import com.example.unscramble.data.allWords
import com.example.unscramble.unscramblerelease.UnscrambleReleaseApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El context debe llamarse dentro una función Composable
// Por eso en el GameViewModel tenemos que pasarlo por parámetro
// Dentro la función Composable en la que llamemos al GameViewModel tenemos que poner ...
// val context = LocalContext.current
class GameViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    companion object {
        // Se crea una instancia de Factory del GameViewModel
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // Funciona igual que init {}
            initializer {
                // Esta variable se le está permitiendo acceder UnscrambleReleaseApplication
                val application = (this[APPLICATION_KEY] as UnscrambleReleaseApplication)
                // Le estamos pasando las preferencias del repositorio al GameViewModel
                GameViewModel(application.userPreferencesRepository)
            }
        }
    }

    // Este estado va cambiando según vaya escribiendo
    var userGuess by mutableStateOf("")
        // Solo permite cambiar el valor de forma interna
        private set

    // Game UI state
    // Setter de uiState
    private val _uiState = MutableStateFlow(GameUiState())
    // Getter de uiState
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Ejemplo de encapsulamiento en Kotlin
    // Setter -> Solo lo utilizamos para cambiar el valor
//    private var _count = 0
    // Si actualizas _count -> count se actualiza también
    // Getter -> Solo muestra el valor de _count
//    val count
//        get() = _count

    // lateinit -> es una variable que se va a inicializar más tarde
    private lateinit var currentWord: String

    // Palabras usadas
    private var usedWords: MutableSet<String> = mutableSetOf()

    init {
        getSettings() // Recoge la configuración
        //setSettings(Language.SPANISH.language, LevelGame.HARD.level) // Crea la configuración del usuario
        //resetGame()
    }

    // Este metodo es para los ajustes del usuario nivel y lenguaje
    fun setSettings(language: String = Language.ENGLISH.language, levelGame: Int = LevelGame.EASY.level) {
        // Comienza la corrutina para settear la configuración
        viewModelScope.launch {
            try {
                // Guarda la configuración desde el repositorio de preferencias del usuario
                userPreferencesRepository.savePreferences(
                    // Este sería el parametro que recibe savePreferences
                    UserPreferences(language, levelGame)
                )

                // Actualiza el estado
                updateStateSettings(language,levelGame)
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al guardar en DataStore")
                _uiState.update {
                    it.copy(
                        // Esto es para guardar el error para poder mostrarlo al usuario
                        userMessages = "Error al guardar en DataStore"
                    )
                }
            }

        }
    }

    private fun getSettings() {
        viewModelScope.launch {
            // Recoge los valores el fichero
            userPreferencesRepository.userPrefs.collect { preferences ->
                // Actualiza la configuración y el estado del usuario
                updateStateSettings(preferences.language, preferences.levelGame)
            }
        }
    }

    // Actualiza los settings y reinicia el juego
    private fun updateStateSettings(language: String, levelGame: Int) {
        usedWords.clear()
        _uiState.update { currentState ->
            currentState.copy(
                currentScrambledWord = pickRandomWordAndShuffle(language),
                maxNoWords = levelGame, // El número de palabras es el nivel del juego
                language = language,
                levelGame = levelGame,
                currentWordCount = 1,
                isGameOver = false,
                isLoading = false,
                isSettingsDialogVisible = false
            )
        }
    }

    // Selecciona 1 palabra aleatoria, sin repetir
    private fun pickRandomWordAndShuffle(language: String): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord =
            if (language == "es") {
                allSpanishWords.random()
            } else {
                allWords.random()
            }
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle(language)
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    // Baraja las letras de la palabra asignada
    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord) == word) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    // Reinicia el juego, es decir borra la lista de palabras usadas
    fun resetGame() {
        updateStateSettings(uiState.value.language, uiState.value.levelGame)
//        usedWords.clear()
//        // Asigna una nueva palabra de forma aleatoria y baraja sus letras
//        _uiState.value = GameUiState(
//            currentScrambledWord = pickRandomWordAndShuffle(),
//            isGameOver = false,
//            score = 0,
//            isGuessedWordWrong = false
//        )
    }

    // Nos permite cambiar el valor de userGuess que tiene el setter privado
    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // Coge la puntuación actual y le incrementa los puntos
            val newScore = uiState.value.score.plus(SCORE_INCREASE)

            // Actualiza el estado de los puntos y selecciona una palabra nueva aleatoria
            updateGameState(newScore)
        } else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    fun skipWord() {
        _uiState.update { currentState ->
            currentState.copy(
                currentScrambledWord = pickRandomWordAndShuffle(currentState.language),
                currentWordCount = currentState.currentWordCount.inc(),
                isGameOver = currentState.currentWordCount >= currentState.levelGame
            )
        }
    }

    private fun updateGameState(score: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                score = score,
                isGuessedWordWrong = false,
                currentScrambledWord = pickRandomWordAndShuffle(currentState.language),
                currentWordCount = currentState.currentWordCount.inc(),
                isGameOver = currentState.currentWordCount >= currentState.levelGame
            )
        }
    }

    fun showSettings() {
        _uiState.update { currentState ->
            currentState.copy(
                isSettingsDialogVisible = true,
            )
        }
    }

    fun hideSettings() {
        _uiState.update { currentState ->
            currentState.copy(
                isSettingsDialogVisible = false,
            )
        }
    }

    // Ejemplo de actualización
    /*
    init {
        _uiState.update {
            it.copy(count = it.count+1)
        }
    }
    */
}