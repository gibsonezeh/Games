package com.gibson.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Define navigation routes
object AppRoutes {
    const val GAME_SELECTION = "game_selection"
    const val LUDO_GAME = "ludo_game"
    // Add other game routes here as needed
}

data class GameItem(val id: String, val name: String, val description: String, val route: String, val thumbnailUrl: String = "")

val availableGames = listOf(
    GameItem(id = "ludo", name = "Ludo Game", description = "Roll your dice and race to the finish!", route = AppRoutes.LUDO_GAME)
    // Add other games here
)

class MainActivity : ComponentActivity() {

    private var appOpenAdManager: AppOpenAdManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appOpenAdManager = (application as MyApplication).appOpenAdManager

        setContent {
            AppNavigation(appOpenAdManager = appOpenAdManager, activity = this)
        }
    }
}

@Composable
fun AppNavigation(appOpenAdManager: AppOpenAdManager?, activity: Activity) {
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle events to show AppOpenAd on RESUME
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                appOpenAdManager?.showAdIfAvailable(activity)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MaterialTheme {
        NavHost(navController = navController, startDestination = AppRoutes.GAME_SELECTION) {
            composable(AppRoutes.GAME_SELECTION) {
                GameSelectionScreen(navController = navController, activity = activity)
            }
            composable(AppRoutes.LUDO_GAME) {
                val ludoViewModel: LudoViewModel = viewModel(factory = LudoViewModelFactory(LocalContext.current.applicationContext))
                LudoGameScreen(viewModel = ludoViewModel, activity = activity) // Pass activity for potential ad contexts
            }
        }
    }
}

@Composable
fun GameSelectionScreen(navController: NavController, activity: Activity) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select a Game") })
        },
        bottomBar = {
            // AdMob Banner Ad
            AdMobBanner(adUnitId = "ca-app-pub-8105096464664625/6118918264", activity = activity)
        }
    ) {\innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Native Ad Placeholder - to be implemented at the top of the game list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Example height for Native Ad
                    .background(Color.LightGray.copy(alpha = 0.5f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Native Advanced Ad Placeholder (Game Selection Screen)", textAlign = TextAlign.Center)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Two columns in the grid
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableGames) { game ->
                    GameCard(game = game, onClick = { navController.navigate(game.route) })
                }
            }
        }
    }
}

@Composable
fun GameCard(game: GameItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for Thumbnail
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("Thumb", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = game.name, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = game.description, style = MaterialTheme.typography.caption, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreviewMainActivity() {
    // For preview, we might need to mock AppOpenAdManager and Activity
    // Or provide a simpler preview that doesn"t rely on them.
    MaterialTheme {
        GameSelectionScreen(navController = rememberNavController(), activity = LocalContext.current as Activity)
    }
}

