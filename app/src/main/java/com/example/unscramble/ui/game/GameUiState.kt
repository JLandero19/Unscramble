package com.example.unscramble.ui.game

import com.example.unscramble.data.Language
import com.example.unscramble.data.LevelGame

data class GameUiState(
    val currentScrambledWord: String = "",
    val isGuessedWordWrong: Boolean = false,
    val score: Int = 0,
    val currentWordCount: Int = 1,
    val isGameOver: Boolean = false,
    val language: String = Language.ENGLISH.language,
    val levelGame: Int = LevelGame.EASY.level,
    val maxNoWords: Int = LevelGame.EASY.level,
    val isLoading: Boolean = true,
    val userMessages: String = ""
)