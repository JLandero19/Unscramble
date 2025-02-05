package com.example.unscramble.localdatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.unscramble.datamodel.GameModel
import kotlinx.coroutines.flow.Flow

@Dao
interface GamesDAO {
    @Insert
    suspend fun insertGame(game: GameModel)

    @Delete
    suspend fun deleteGame(game: GameModel)

    @Update
    suspend fun update(game: GameModel)

    @Query("SELECT * from games ORDER BY date")
    fun getAllGames(): Flow<List<GameModel>>

    @Query("SELECT * from games ORDER BY score DESC")
    fun getRankingGames(): Flow<List<GameModel>>

    @Query("DELETE FROM games")
    suspend fun clearGames()

}