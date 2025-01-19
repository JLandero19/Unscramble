package com.example.unscramble.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allSpanishWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// El context debe llamarse dentro una función Composable
// Por eso en el GameViewModel tenemos que pasarlo por parámetro
// Dentro la función Composable en la que llamemos al GameViewModel tenemos que poner ...
// val context = LocalContext.current
class GameViewModel : ViewModel() {

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
    private var _count = 0
    // Si actualizas _count -> count se actualiza también
    // Getter -> Solo muestra el valor de _count
    val count
        get() = _count

    // lateinit -> es una variable que se va a inicializar más tarde
    private lateinit var currentWord: String

    // Palabras usadas
    private var usedWords: MutableSet<String> = mutableSetOf()

    init {
        resetGame()
    }

    // Selecciona 1 palabra aleatoria, sin repetir
    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allSpanishWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
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
        usedWords.clear()
        // Asigna una nueva palabra de forma aleatoria y baraja sus letras
        _uiState.value = GameUiState(
            currentScrambledWord = pickRandomWordAndShuffle(),
            isGameOver = false,
            score = 0,
            isGuessedWordWrong = false
        )
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
                currentScrambledWord = pickRandomWordAndShuffle(),
                currentWordCount = currentState.currentWordCount.inc(),
                isGameOver = currentState.currentWordCount >= MAX_NO_OF_WORDS
            )
        }
    }

    private fun updateGameState(score: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                score = score,
                isGuessedWordWrong = false,
                currentScrambledWord = pickRandomWordAndShuffle(),
                currentWordCount = currentState.currentWordCount.inc(),
                isGameOver = currentState.currentWordCount >= MAX_NO_OF_WORDS
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