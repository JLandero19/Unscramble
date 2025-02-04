package com.example.unscramble.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "score")
    val score: Int,
    @ColumnInfo(name = "right_words")
    val rightWords: String,
    @ColumnInfo(name = "wrong_words")
    val wrongWords: String
)
