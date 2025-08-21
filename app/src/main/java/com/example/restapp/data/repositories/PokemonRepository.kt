package com.example.restapp.data.repositories

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.restapp.data.dao.PokemonDetailsDao
import com.example.restapp.data.dao.PokemonListDao
import com.example.restapp.data.entities.PokemonDetailsEntity
import com.example.restapp.data.entities.PokemonListEntity
import com.example.restapp.data.models.PokemonDetails
import com.example.restapp.data.models.PokemonListItem
import com.example.restapp.data.models.PokemonType
import com.example.restapp.data.models.Sprites
import com.example.restapp.service.PokeApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

class PokemonRepository(
    private val pokeApiService: PokeApiService,
    private val pokemonListDao: PokemonListDao,
    private val pokemonDetailsDao: PokemonDetailsDao,
    private val connectivityManager: ConnectivityManager,
    private val context: Context
) {
    // Для списка покемонов
    fun getPokemonList(forceRefresh: Boolean = false): Flow<Resource<List<PokemonListItem>>> = flow {
        emit(Resource.Loading())

        // Пробуем получить данные из БД
        val localPokemon = pokemonListDao.getAllPokemon().first()

        if (localPokemon.isNotEmpty() && !forceRefresh) {
            emit(Resource.Success(localPokemon.map {
                PokemonListItem(it.name, it.url)
            }))
        }

        // Пробуем обновить данные из сети
        try {
            val remotePokemon = pokeApiService.getPokemonList()
            pokemonListDao.clearAll()
            pokemonListDao.insertAll(
                remotePokemon.results.map {
                    PokemonListEntity(it.name, it.url)
                }
            )
            emit(Resource.Success(remotePokemon.results))
        } catch (e: Exception) {
            if (localPokemon.isEmpty()) {
                emit(Resource.Error("No cached data available"))
            } else {
                emit(Resource.Error("Using cached data",
                    localPokemon.map { PokemonListItem(it.name, it.url) }))
            }
        }
    }

    // Для деталей покемона
    fun getPokemonDetails(name: String): Flow<Resource<PokemonDetails>> = flow {
        emit(Resource.Loading())

        val cachedPokemon = pokemonDetailsDao.getPokemonByName(name).first()
        if (cachedPokemon != null) {
            emit(Resource.Success(mapToPokemonDetails(cachedPokemon)))
        }

        // Пробуем загрузить из сети (если есть подключение)
        if (isNetworkAvailable()) {
            try {
                val remotePokemon = pokeApiService.getPokemonDetails(name)
                val typesJson = Gson().toJson(remotePokemon.types)

                pokemonDetailsDao.insert(
                    PokemonDetailsEntity(
                        id = remotePokemon.id,
                        name = remotePokemon.name,
                        height = remotePokemon.height,
                        weight = remotePokemon.weight,
                        imageUrl = remotePokemon.sprites.frontDefault,
                        types = typesJson
                    )
                )

                // Проверка что данные сохранились
                val count = pokemonDetailsDao.getCount()
                Log.d("DB", "Total pokemons in DB: $count")

                emit(Resource.Success(remotePokemon))
            } catch (e: Exception) {
                // Если есть кэш - показываем его с сообщением
                val cachedPokemon = pokemonDetailsDao.getPokemonByName(name).first()
                if (cachedPokemon != null) {
                    emit(Resource.Error(
                        message = "Network error. Showing cached data",
                        data = mapToPokemonDetails(cachedPokemon)
                    ))
                    return@flow
                }
                emit(Resource.Error("No data available"))
            }
        } else {
            // Если нет интернета и нет кэша
            if (pokemonDetailsDao.getPokemonByName(name).first() == null) {
                emit(Resource.Error("No internet connection and no cached data"))
            }
        }
    }

    private fun mapToPokemonDetails(entity: PokemonDetailsEntity): PokemonDetails {
        val types = try {
            Gson().fromJson<List<PokemonType>>(
                entity.types,
                object : TypeToken<List<PokemonType>>() {}.type
            )
        } catch (e: Exception) {
            emptyList()
        }

        return PokemonDetails(
            id = entity.id,
            name = entity.name,
            height = entity.height,
            weight = entity.weight,
            sprites = Sprites(entity.imageUrl),
            types = types
        )
    }

    suspend fun isNetworkAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        }
    }
}