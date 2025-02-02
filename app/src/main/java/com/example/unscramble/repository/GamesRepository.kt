package com.example.unscramble.repository

import com.example.unscramble.datamodel.GameModel
import com.example.unscramble.localdatabase.GamesDAO
import kotlinx.coroutines.flow.Flow

class GamesRepository(
    private val gamesDAO: GamesDAO
) : GamesInterface {

    override suspend fun insertGame(game: GameModel) = gamesDAO.insertGame(game)

    override suspend fun deleteGame(game: GameModel) = gamesDAO.deleteGame(game)

    override suspend fun update(game: GameModel) = gamesDAO.update(game)

    override val getAllGames: Flow<List<GameModel>> = gamesDAO.getAllGames()

    override suspend fun clearGames() = gamesDAO.clearGames()

}