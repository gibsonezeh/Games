package com.gibson.games

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gibson.games.candycrush.CandyCrushScreen

@Composable
fun GamesApp() {
    var selectedGame by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (selectedGame) {
                "candy_crush" -> CandyCrushScreen(onBack = { selectedGame = null })
                else -> GameMenu(onGameSelected = { selectedGame = it })
            }
        }
    }
}

@Composable
fun GameMenu(onGameSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎮 Multi Game App", fontSize = 28.sp)
        Button(onClick = { onGameSelected("candy_crush") }) {
            Text("🍬 Play Candy Crush")
        }
        Button(onClick = { }, enabled = false) {
            Text("🧱 Tetris (Coming Soon)")
        }
    }
}
