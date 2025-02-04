package com.example.unscramble.localdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.unscramble.datamodel.GameModel
import com.example.unscramble.datamodel.WordModel

@Database(entities = [GameModel::class, WordModel::class], version = 2, exportSchema = false)
abstract class UnscrambleDatabase : RoomDatabase() {

    abstract fun gamesDAO(): GamesDAO
    abstract fun wordsDAO(): WordsDAO

    companion object {
        @Volatile
        private var Instance: UnscrambleDatabase? = null
        fun getDatabase(context: Context): UnscrambleDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, UnscrambleDatabase::class.java, "unscramble_database")
                    .fallbackToDestructiveMigration() // Cuando se realice un cambio en la base de datos, se borran los datos
                    .build()
                    .also { Instance = it }
            }
        }
    }
}