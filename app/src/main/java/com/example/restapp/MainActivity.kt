package com.example.restapp

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.safe.args.generator.ErrorMessage
import coil.compose.AsyncImage
import com.example.restapp.data.PokemonViewModel
import com.example.restapp.data.models.PokemonDetails
import com.example.restapp.data.models.PokemonListItem
import com.example.restapp.data.repositories.Resource
import com.example.restapp.ui.theme.RestAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestAPPTheme {
                PokemonApp()
            }
        }
    }
}

@Composable
fun rememberPokemonViewModel(): PokemonViewModel {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PokemonViewModel(application) as T
            }
        }
    )
}

//@Composable
//fun PokemonApp() {
//    val navController = rememberNavController()
//    val viewModel: PokemonViewModel = viewModel()
//
//    NavHost(navController = navController, startDestination = "list") {
//        composable("list") {
//            PokemonListScreen(
//                pokemonList = viewModel.pokemonList.value,
//                isLoading = viewModel.isLoading.value,
//                error = viewModel.error.value,
//                isNetworkAvailable = viewModel.isNetworkAvailable.value,
//                onRefresh = { viewModel.loadPokemonList() },
//                onPokemonClick = { name ->
//                    navController.navigate("details/$name")
//                }
//            )
//        }
//        composable("details/{name}") { backStackEntry ->
//            val name = backStackEntry.arguments?.getString("name") ?: return@composable
//            if (viewModel.selectedPokemon.value?.name != name) {
//                viewModel.loadPokemonDetails(name)
//            }
//
//            PokemonDetailsScreen(
//                pokemon = viewModel.selectedPokemon.value,
//                isLoading = viewModel.isLoading.value,
//                error = viewModel.error.value,
//                onBack = { navController.popBackStack() }
//            )
//        }
//    }
//}

@Composable
fun PokemonApp() {
    val navController = rememberNavController()
    val viewModel = rememberPokemonViewModel()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            PokemonListScreen(
                viewModel = viewModel,
                onPokemonClick = { name ->
                    navController.navigate("details/$name")
                }
            )
        }
        composable("details/{name}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: return@composable
            PokemonDetailsScreen(
                name = name,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PokemonListScreen(
//    pokemonList: List<PokemonListItem>,
//    isLoading: Boolean,
//    error: String?,
//    isNetworkAvailable: Boolean,
//    onRefresh: () -> Unit,
//    onPokemonClick: (String) -> Unit
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text("Pokémon List") })
//        }
//    ) { padding ->
//        Box(modifier = Modifier.padding(padding)) {
//            if (!isNetworkAvailable) {
//                NetworkErrorBanner()
//            }
//
//            if (error != null) {
//                ErrorMessage(error = error, onRetry = onRefresh)
//            } else if (isLoading && pokemonList.isEmpty()) {
//                LoadingIndicator()
//            } else {
//                LazyColumn {
//                    items(pokemonList) { pokemon ->
//                        PokemonListItem(pokemon = pokemon, onClick = onPokemonClick)
//                    }
//
//                    if (isLoading && pokemonList.isNotEmpty()) {
//                        item { LoadingItem() }
//                    }
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    viewModel: PokemonViewModel,
    onPokemonClick: (String) -> Unit
) {
    val pokemonListState = viewModel.pokemonListState.value
    val isNetworkAvailable by viewModel.isNetworkAvailable

    LaunchedEffect(Unit) {
        viewModel.loadPokemonList()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pokémon List") }) },
        floatingActionButton = {
            if (isNetworkAvailable) {
                FloatingActionButton(onClick = { viewModel.loadPokemonList(true) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (pokemonListState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Success -> {
                    LazyColumn {
                        items(pokemonListState.data ?: emptyList()) { pokemon ->
                            PokemonListItem(pokemon, onClick = onPokemonClick)
                        }
                    }
                }
                is Resource.Error -> {
                    // Вот здесь ключевое изменение:
                    Column {
                        if (pokemonListState.data != null) {
                            LazyColumn {
                                items(pokemonListState.data) { pokemon ->
                                    PokemonListItem(pokemon, onClick = onPokemonClick)
                                }
                            }
                            Text(
                                text = pokemonListState.message ?: "Using cached data",
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            ErrorMessage(
                                error = pokemonListState.message ?: "Unknown error",
                                onRetry = { viewModel.loadPokemonList(true) }
                            )
                        }
                    }
                }
            }

            if (!isNetworkAvailable) {
                NetworkErrorBanner(isVisible = !isNetworkAvailable)
            }
        }
    }
}

@Composable
fun PokemonListItem(pokemon: PokemonListItem, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(pokemon.name) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NetworkErrorBanner(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(8.dp)
        ) {
            Text(
                text = "No internet connection",
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PokemonDetailsScreen(
//    pokemon: PokemonDetails?,
//    isLoading: Boolean,
//    error: String?,
//    onBack: () -> Unit
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(pokemon?.name?.replaceFirstChar { it.uppercase() } ?: "Pokémon Details")
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Box(modifier = Modifier.padding(padding)) {
//            if (isLoading && pokemon == null) {
//                LoadingIndicator()
//            } else if (error != null) {
//                ErrorMessage(error = error, onRetry = null)
//            } else if (pokemon != null) {
//                PokemonDetailsContent(pokemon = pokemon)
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailsScreen(
    name: String,
    viewModel: PokemonViewModel,
    onBack: () -> Unit
) {
    val pokemonDetailsState = viewModel.pokemonDetailsState.value

    LaunchedEffect(name) {
        viewModel.loadPokemonDetails(name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokémon Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (pokemonDetailsState) {
                is Resource.Loading -> LoadingIndicator()
                is Resource.Success -> {
                    pokemonDetailsState.data?.let { pokemon ->
                        PokemonDetailsContent(pokemon = pokemon)
                    }
                }
                is Resource.Error -> {
                    // Показываем кэшированные данные, если они есть
                    if (pokemonDetailsState.data != null) {
                        Column {
                            PokemonDetailsContent(pokemon = pokemonDetailsState.data!!)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = pokemonDetailsState.message ?: "Using cached data",
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(
                                onClick = { viewModel.loadPokemonDetails(name) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Retry")
                            }
                        }
                    } else {
                        ErrorMessage(
                            error = pokemonDetailsState.message ?: "Unknown error",
                            onRetry = { viewModel.loadPokemonDetails(name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonDetailsContent(pokemon: PokemonDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = pokemon.sprites.frontDefault,
            contentDescription = pokemon.name,
            modifier = Modifier.size(200.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "#${pokemon.id} ${pokemon.name.replaceFirstChar { it.uppercase() }}")

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("Height: ${pokemon.height / 10.0}m", modifier = Modifier.padding(end = 16.dp))
            Text("Weight: ${pokemon.weight / 10.0}kg")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Types:")
        Row {
            pokemon.types.forEach { type ->
                Chip(label = type.type.name.replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun Chip(label: String) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label)
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

//@Composable
//fun LoadingItem() {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        CircularProgressIndicator()
//    }
//}

@Composable
fun ErrorMessage(error: String, onRetry: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = error)

        Spacer(modifier = Modifier.height(8.dp))

        if (onRetry != null) {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

class NetworkChangeReceiver(private val onNetworkAvailable: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        onNetworkAvailable(networkInfo != null && networkInfo.isConnected)
    }
}

//@Composable
//fun rememberNetworkState(): State<Boolean> {
//    val context = LocalContext.current
//    val isOnline = remember { mutableStateOf(true) }
//
//    DisposableEffect(context) {
//        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        val receiver = NetworkChangeReceiver { isAvailable ->
//            isOnline.value = isAvailable
//        }
//
//        context.registerReceiver(receiver, intentFilter)
//
//        onDispose {
//            context.unregisterReceiver(receiver)
//        }
//    }
//
//    return isOnline
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RestAPPTheme {
        PokemonApp()
    }
}