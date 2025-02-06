package com.example.unscramble.ui.Ranking

import com.example.unscramble.datamodel.GameModel

data class RankingUiState(
    val ranking: List<GameModel> = mutableListOf(),
)