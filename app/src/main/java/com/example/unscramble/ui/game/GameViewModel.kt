package com.example.unscramble.ui.Game

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
import com.example.unscramble.datamodel.WordModel
import com.example.unscramble.localdatabase.WordsDAO
import com.example.unscramble.repository.WordsRepository
import com.example.unscramble.unscramblerelease.UnscrambleReleaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val wordsRepository: WordsRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as UnscrambleReleaseApplication)
                GameViewModel(application.userPreferencesRepository, application.wordsRepository)
            }
        }
    }

    /*
    Si el estado solamente depende del origen de datos, como en el caso de que toma un flujo y este se modifica solamente desde este origen,
    se puede convertir el flujo en un StateFlow, para ello hacemos uso de StateIn.

    val uiState: StateFlow<GameUiState> = userPreferencesRepository.userPrefs
       .map { preferences ->
            GameUiState(
                language = preferences.language,
                levelGame = preferences.levelGame,
                isLoading = false,
                errorMessage = null
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameUiState()
        )
     */

    val getEnglishWords : Flow<List<WordModel>> = wordsRepository.getAllWordsByLanguage(Language.ENGLISH.toString())
    val getSpanishWords : Flow<List<WordModel>> = wordsRepository.getAllWordsByLanguage(Language.SPANISH.toString())


    //Estado de la interfaz de usuario.
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    //Valor de la palabra ingresada por el usuario.
    var userGuess by mutableStateOf("")
        private set

    //Bloque que se ejecuta con la creación del objeto.
    init {
        viewModelScope.launch {
            // Flujo de estado transformado de lista de WordModel a lista de strings.
            val wordsFlow: StateFlow<List<String>> =
                userPreferencesRepository.userPrefs
                    // Toma el flujo de preferencias y lo mapea a un flujo de lista de WordModel.
                    // Cada ver que haya un cambio en las preferencias, se obtendrán nuevos WordModel.
                    .flatMapLatest { preferences ->
                        wordsRepository.getSomeRandomWordsByLanguage(
                            preferences.language,
                            preferences.levelGame
                        )
                    }
                    // Transforma la lista de WordModel en una lista de strings.
                    .map { wordList ->
                        wordList.map { it.title }
                    }
                    // Captura excepciones y actualiza el estado de la interfaz de usuario.
                    .catch { e ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                userMessage = UserMessage.ERROR_GETTING_WORDS,
                                isLoading = false
                            )
                        }
                        emit(emptyList()) // Emitir una lista vacía en caso de error
                    }
                    // Convierte el flujo en un flujo de estado con la lista de palabras.
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )

            // Combina preferencias y lista de palabras en un solo flujo de estado.
            userPreferencesRepository.userPrefs
                .combine(wordsFlow) { preferences, words ->
                    Pair(preferences, words)
                }
                // Inicialmente muestra un estado de carga.
                .onStart {
                    _uiState.update { currentState ->
                        currentState.copy(isLoading = true)
                    }
                }
                // Captura excepciones y actualiza el estado de la interfaz de usuario.
                .catch { e ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            userMessage = UserMessage.ERROR_ACCESSING_DATASTORE,
                            isLoading = false
                        )
                    }
                }
                // Actualiza el estado de la interfaz de usuario con las preferencias y las palabras.
                .collect { (preferences, words) ->
                    Log.d("GameViewModel", "Entra a CollectCombined: $words")
                    // Si no hay palabras, actualiza el estado de la interfaz de usuario con un mensaje de error.
                    if (words.isEmpty()) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                userMessage = UserMessage.ERROR_GETTING_WORDS,
                                isLoading = false
                            )
                        }
                    } else {
                        //Actualiza el estado de la interfaz con las palabras del flujo y las opciones de preferencia.
                        updateState(words.toMutableList(), preferences.language, preferences.levelGame)
                    }
                }
            if (wordsRepository.getAllWords.count() <= 0) {
                // Me inserta en la base de datos las palabras
                insertWords(allWords)
                insertWords(allSpanishWords, "es")
            }
        }

    }
    // Me inserta las palabras en la base de datos
    private fun insertWords(allWords: Set<String>, language: String = "en") {
        viewModelScope.launch {
            val wordList = allWords.map { WordModel(title = it, language = language) }
            wordsRepository.insertWordList(wordList)
        }

    }

    private fun updateState(wordsGame: MutableList<String>,language: String, levelGame: Int) {
        val nextWord = if(wordsGame.isNotEmpty()) wordsGame.removeAt(0) else ""
        _uiState.value = if(wordsGame.isNotEmpty())
            GameUiState(
                wordsGame = wordsGame,
                currentWord = nextWord,
                currentScrambledWord = shuffleCurrentWord(nextWord),
                usedWords = mutableListOf(nextWord),
                language = language,
                levelGame = levelGame,
                isLoading = false,
            ) else GameUiState(isGameOver = true)
        Log.d("GameViewModel", uiState.value.toString())
    }

    fun resetGame() {
        //updateStateSettings(uiState.value.language, uiState.value.levelGame)
        viewModelScope.launch {
            val wordList = wordsRepository.getOnceSomeRandomWordsByLanguage(uiState.value.language, uiState.value.levelGame)
            updateState(wordList.map { it.title }.toMutableList(), uiState.value.language, uiState.value.levelGame)
        }
    }

    private fun updateGameState(updatedScore: Int) {
        if (uiState.value.wordsGame.isEmpty()){
            //Last round in the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                val nextWord = currentState.wordsGame.first()
                currentState.copy(
                    wordsGame = currentState.wordsGame.apply { removeAt(0) },
                    isGuessedWordWrong = false,
                    usedWords = currentState.usedWords.apply { add(nextWord) },
                    currentWord = nextWord,
                    currentScrambledWord = shuffleCurrentWord(nextWord),
                    score = updatedScore
                )
            }
        }
        Log.d("GameViewModel", uiState.value.toString())
    }

    fun setSettings(language: String = Language.ENGLISH.language, levelGame: Int = LevelGame.EASY.level) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.savePreferences(
                    UserPreferences(language, levelGame)
                )
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        userMessage = UserMessage.ERROR_WRITING_DATASTORE
                    )
                }
            }
        }
    }

    private fun pickRandomWord(language: String): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        val word = if(language == Language.SPANISH.language) allSpanishWords.random() else allWords.random()
        return if (uiState.value.usedWords.contains(word)) {
            pickRandomWord(language)
        } else {
            word
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord) == word) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    private fun updateStateSettings(language: String, levelGame: Int) {
        val word = pickRandomWord(language)
        _uiState.update { currentState ->
            currentState.copy(
                currentWord = word,
                currentScrambledWord = shuffleCurrentWord(word),
                usedWords = mutableListOf(word),
                language = language,
                levelGame = levelGame,
                score = 0,
                isLoading = false,
                isGameOver = false
            )
        }
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(uiState.value.currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            // Si falla muestra un error.
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        //Borra el texto.
        updateUserGuess("")
    }
}