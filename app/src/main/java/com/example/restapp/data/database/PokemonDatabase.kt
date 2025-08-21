package com.example.restapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.restapp.data.dao.PokemonDetailsDao
import com.example.restapp.data.dao.PokemonListDao
import com.example.restapp.data.entities.PokemonDetailsEntity
import com.example.restapp.data.entities.PokemonListEntity

@Database(
    entities = [PokemonListEntity::class, PokemonDetailsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun pokemonListDao(): PokemonListDao
    abstract fun pokemonDetailsDao(): PokemonDetailsDao

    companion object {
        const val DATABASE_NAME = "pokemon.db"

        @Volatile
        private var INSTANCE: PokemonDatabase? = null

        fun getDatabase(context: Context): PokemonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PokemonDatabase::class.java,
                    "pokemon_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}