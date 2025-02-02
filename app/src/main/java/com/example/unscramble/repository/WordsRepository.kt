package com.example.unscramble.repository

import com.example.unscramble.datamodel.WordModel
import com.example.unscramble.localdatabase.WordsDAO
import kotlinx.coroutines.flow.Flow

class WordsRepository(
    private val wordsDAO: WordsDAO
)  : WordsInterface {

    override suspend fun insert(word: WordModel)  = wordsDAO.insert(word)

    suspend fun insertWordList(wordList: List<WordModel>)  = wordsDAO.insertWordList(wordList)

    override suspend fun update(word: WordModel) = wordsDAO.update(word)

    override suspend fun delete(word: WordModel) = wordsDAO.delete(word)

    override suspend fun clearWords() = wordsDAO.clearWords()

    override val getAllWords: Flow<List<WordModel>> = wordsDAO.getAllWords()

    override val getSomeRandomWords: (Int) -> Flow<List<WordModel>> = { wordsDAO.getSomeRandomWords(it) }

    override val getAllWordsByLanguage: (String) -> Flow<List<WordModel>> =
        { wordsDAO.getAllWordsByLanguage(it) }

    override val getSomeRandomWordsByLanguage: (String, Int) -> Flow<List<WordModel>> =
        { language, number -> wordsDAO.getSomeRandomWordsByLanguage(language, number) }

    override suspend fun getOnceSomeRandomWordsByLanguage(language: String, number: Int) : List<WordModel> =
        wordsDAO.getOnceSomeRandomWordsByLanguage(language, number)
}