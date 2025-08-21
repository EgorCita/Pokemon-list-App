package com.example.restapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_list")
data class PokemonListEntity(
    @PrimaryKey val name: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pokemon_details")
data class PokemonDetailsEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val imageUrl: String,
    val types: String, // Будем хранить типы как JSON строку
    val timestamp: Long = System.currentTimeMillis()
)