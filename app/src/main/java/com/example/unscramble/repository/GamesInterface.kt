package com.example.unscramble.repository

import com.example.unscramble.datamodel.GameModel
import kotlinx.coroutines.flow.Flow

interface GamesInterface {
    suspend fun insertGame(game: GameModel)

    suspend fun deleteGame(game: GameModel)

    suspend fun update(game: GameModel)

    val getAllGames: Flow<List<GameModel>>

    suspend fun clearGames()
}