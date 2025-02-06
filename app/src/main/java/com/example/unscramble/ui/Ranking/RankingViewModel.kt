package com.example.unscramble.ui.Ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.unscramble.repository.GamesRepository
import com.example.unscramble.unscramblerelease.UnscrambleReleaseApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel (
    private val gamesRepository: GamesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as UnscrambleReleaseApplication)
                RankingViewModel(application.gamesRepository)
            }
        }
    }

    init {
        viewModelScope.launch {
            rankingGames()
        }
    }

    private suspend fun rankingGames() {
        _uiState.update { currentState ->
            currentState.copy(
                ranking = gamesRepository.getRankingGames.first()
            )
        }
    }
}

