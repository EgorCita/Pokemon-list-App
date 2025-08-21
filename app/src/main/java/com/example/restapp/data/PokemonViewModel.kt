package com.example.restapp.data

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restapp.data.database.PokemonDatabase
import com.example.restapp.data.models.PokemonDetails
import com.example.restapp.data.models.PokemonListItem
import com.example.restapp.data.repositories.PokemonRepository
import com.example.restapp.data.repositories.Resource
import com.example.restapp.service.PokeApi
import kotlinx.coroutines.launch

class PokemonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PokemonRepository
    private val _pokemonListState = mutableStateOf<Resource<List<PokemonListItem>>>(Resource.Loading())
    val pokemonListState: State<Resource<List<PokemonListItem>>> = _pokemonListState

    private val _pokemonDetailsState = mutableStateOf<Resource<PokemonDetails>>(Resource.Loading())
    val pokemonDetailsState: State<Resource<PokemonDetails>> = _pokemonDetailsState

    private val _isNetworkAvailable = mutableStateOf(true)
    val isNetworkAvailable: State<Boolean> = _isNetworkAvailable

    private val connectivityManager =
        getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        val db = PokemonDatabase.getDatabase(application)
        repository = PokemonRepository(
            pokeApiService = PokeApi.service,
            pokemonListDao = db.pokemonListDao(),
            pokemonDetailsDao = db.pokemonDetailsDao(),
            connectivityManager = connectivityManager,
            context = application
        )
        checkNetworkConnection()
        loadPokemonList()
    }

    private fun setupNetworkCallback() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isNetworkAvailable.value = true
            }

            override fun onLost(network: Network) {
                _isNetworkAvailable.value = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun loadPokemonList(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            repository.getPokemonList(forceRefresh && isNetworkAvailable.value)
                .collect { result ->
                    _pokemonListState.value = result
                }
        }
    }

    fun loadPokemonDetails(name: String) {
        viewModelScope.launch {
            repository.getPokemonDetails(name)
                .collect { result ->
                    _pokemonDetailsState.value = result
                }
        }
    }

    private fun checkNetworkConnection() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        _isNetworkAvailable.value = capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}

//class PokemonViewModel(application: Application) : AndroidViewModel(application) {
//    private val _pokemonList = mutableStateOf<List<PokemonListItem>>(emptyList())
//    val pokemonList: State<List<PokemonListItem>> = _pokemonList
//
//    private val _selectedPokemon = mutableStateOf<PokemonDetails?>(null)
//    val selectedPokemon: State<PokemonDetails?> = _selectedPokemon
//
//    private val _isLoading = mutableStateOf(false)
//    val isLoading: State<Boolean> = _isLoading
//
//    private val _error = mutableStateOf<String?>(null)
//    val error: State<String?> = _error
//
//    private val _isNetworkAvailable = mutableStateOf(true)
//    val isNetworkAvailable: State<Boolean> = _isNetworkAvailable
//
//    init {
//        checkNetworkConnection()
//        loadPokemonList()
//    }
//
//    fun loadPokemonList() {
//        if (!_isNetworkAvailable.value) {
//            _error.value = "No internet connection"
//            return
//        }
//
//        _isLoading.value = true
//        viewModelScope.launch {
//            try {
//                val response = PokeApi.service.getPokemonList()
//                _pokemonList.value = response.results
//                _error.value = null
//            } catch (e: Exception) {
//                _error.value = "Failed to load Pokémon: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun loadPokemonDetails(name: String) {
//        if (!_isNetworkAvailable.value) {
//            _error.value = "No internet connection"
//            return
//        }
//
//        _isLoading.value = true
//        viewModelScope.launch {
//            try {
//                _selectedPokemon.value = PokeApi.service.getPokemonDetails(name)
//                _error.value = null
//            } catch (e: Exception) {
//                _error.value = "Failed to load Pokémon details: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    private fun checkNetworkConnection() {
//        val connectivityManager = getApplication<Application>()
//            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        // Для Android 7+ (API 24+) используем NetworkCapabilities
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            val network = connectivityManager.activeNetwork
//            val capabilities = connectivityManager.getNetworkCapabilities(network)
//            _isNetworkAvailable.value = capabilities != null &&
//                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
//                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
//        } else {
//            // Устаревший способ для старых версий Android
//            val networkInfo = connectivityManager.activeNetworkInfo
//            _isNetworkAvailable.value = networkInfo != null && networkInfo.isConnected
//        }
//    }
//}