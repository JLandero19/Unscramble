package com.example.unscramble.ui.game

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.getWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// El context debe llamarse dentro una función Composable
// Por eso en el GameViewModel tenemos que pasarlo por parámetro
// Dentro la función Composable en la que llamemos al GameViewModel tenemos que poner ...
// val context = LocalContext.current
class GameViewModel(context: Context) : ViewModel() {

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
        get() = "Mi contador es $_count"

    // lateinit -> es una variable que se va a inicializar más tarde
    private lateinit var currentWord: String

    // Palabras usadas
    private var usedWords: MutableSet<String> = mutableSetOf()

    init {
        resetGame(context)
    }

    // Selecciona 1 palabra aleatoria, sin repetir
    private fun pickRandomWordAndShuffle(context: Context): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = getWords(context).random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle(context)
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    // Baraja las letras de la palabra
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
    fun resetGame(context: Context) {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle(context))
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