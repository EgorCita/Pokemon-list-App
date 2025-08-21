package com.example.restapp.data.models

import com.google.gson.annotations.SerializedName

data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>
)

data class PokemonListItem(
    val name: String,
    val url: String
)

data class PokemonDetails(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<PokemonType>
)

data class Sprites(
    @SerializedName("front_default") val frontDefault: String
)

data class PokemonType(
    val slot: Int,
    val type: Type
)

data class Type(
    val name: String
)