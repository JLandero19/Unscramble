package com.example.unscramble.localdatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.unscramble.datamodel.WordModel
import kotlinx.coroutines.flow.Flow

@Dao
interface WordsDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(word: WordModel)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWordList(wordList: List<WordModel>)

    @Update
    suspend fun update(word: WordModel)

    @Delete
    suspend fun delete(word: WordModel)

    @Query("DELETE FROM words")
    suspend fun clearWords()

    @Query("SELECT * FROM words ORDER BY title ASC")
    fun getAllWords(): Flow<List<WordModel>>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :number")
    fun getSomeRandomWords(number: Int): Flow<List<WordModel>>

    @Query("SELECT * FROM words WHERE language = :language")
    fun getAllWordsByLanguage(language: String): Flow<List<WordModel>>

    @Query("SELECT * FROM words WHERE language = :language ORDER BY RANDOM() LIMIT :number")
    fun getSomeRandomWordsByLanguage(language: String, number: Int): Flow<List<WordModel>>

    @Query("SELECT * FROM words WHERE language = :language ORDER BY RANDOM() LIMIT :number")
    suspend fun getOnceSomeRandomWordsByLanguage(language: String, number: Int): List<WordModel>
}