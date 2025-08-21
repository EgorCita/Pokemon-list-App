package com.example.restapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.restapp.data.entities.PokemonDetailsEntity
import com.example.restapp.data.entities.PokemonListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemon: List<PokemonListEntity>)

    @Query("SELECT * FROM pokemon_list ORDER BY name ASC")
    fun getAllPokemon(): Flow<List<PokemonListEntity>>

    @Query("DELETE FROM pokemon_list")
    suspend fun clearAll()
}

@Dao
interface PokemonDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pokemon: PokemonDetailsEntity)

    @Query("SELECT * FROM pokemon_details WHERE id = :id")
    fun getPokemonById(id: Int): Flow<PokemonDetailsEntity?>

    @Query("SELECT * FROM pokemon_details WHERE name = :name")
    fun getPokemonByName(name: String): Flow<PokemonDetailsEntity?>

    @Query("DELETE FROM pokemon_details WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM pokemon_details")
    suspend fun getCount(): Int
}