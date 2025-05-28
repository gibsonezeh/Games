package com.gibson.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gibson.games.dreamtreats.DreamTreatsGameScreen
// import other game screens here as you build them

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiGameApp()
        }
    }
}

@Composable
fun MultiGameApp() {
    var currentGame by remember { mutableStateOf<GameOption?>(null) }

    when (currentGame) {
        null -> GameMenuScreen(onGameSelected = { selected -> currentGame = selected })
        GameOption.DREAM_TREATS -> DreamTreatsGameScreen()
        // Add more cases as you implement other games
    }
}

enum class GameOption {
    DREAM_TREATS,
    // Add more game identifiers here: TIC_TAC_TOE, MEMORY_GAME, etc.
}

@Composable
fun GameMenuScreen(onGameSelected: (GameOption) -> Unit) {
    Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎮 Game Hub", style = MaterialTheme.typography.h4, color = Color(0xFF3F51B5))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onGameSelected(GameOption.DREAM_TREATS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🍬 Dream Treats")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Future Games:
            /*
            Button(onClick = { onGameSelected(GameOption.TIC_TAC_TOE) }) {
                Text("❌ Tic Tac Toe")
            }

            Button(onClick = { onGameSelected(GameOption.MEMORY_GAME) }) {
                Text("🧠 Memory Match")
            }
            */
        }
    }
}
