package com.example.unscramble.repository

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.unscramble.datamodel.WordModel
import kotlinx.coroutines.flow.Flow

interface WordsInterface {
    suspend fun insert(word: WordModel)

    suspend fun update(word: WordModel)

    suspend fun delete(word: WordModel)

    suspend fun clearWords()

    val getAllWords : Flow<List<WordModel>>

    val getSomeRandomWords : (Int) -> Flow<List<WordModel>>

    val getAllWordsByLanguage : (String) -> Flow<List<WordModel>>

    val getSomeRandomWordsByLanguage : (String, Int) -> Flow<List<WordModel>>

    suspend fun getOnceSomeRandomWordsByLanguage(language: String, number: Int) : List<WordModel>

}