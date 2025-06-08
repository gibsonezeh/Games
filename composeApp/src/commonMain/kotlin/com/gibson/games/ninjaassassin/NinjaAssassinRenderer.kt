package com.gibson.games.ninjaassassin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun NinjaAssassinGameScreen(onExit: () -> Unit) {
    val game = remember { NinjaAssassinGame() }

    // Game loop
    LaunchedEffect(Unit) {
        while (true) {
            game.update()
            delay(16L)
        }
    }

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Ninja Assassin", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onExit) { Text("Back") }
        }

        Box(Modifier.fillMaxSize()) {
            // Drawing canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background
                drawRect(Color.DarkGray)

                // Player
                drawRect(
                    color = Color.Green,
                    topLeft = androidx.compose.ui.geometry.Offset(game.player.x, game.player.y),
                    size = androidx.compose.ui.geometry.Size(game.player.size, game.player.size)
                )

                // Enemy
                drawRect(
                    color = Color.Red,
                    topLeft = androidx.compose.ui.geometry.Offset(game.enemy.x, game.enemy.y),
                    size = androidx.compose.ui.geometry.Size(game.enemy.size, game.enemy.size)
                )
            }

            // On-screen movement controls (for now)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Button(onClick = { game.movePlayer(-10f, 0f) }) { Text("←") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { game.movePlayer(0f, -10f) }) { Text("↑") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { game.movePlayer(0f, 10f) }) { Text("↓") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { game.movePlayer(10f, 0f) }) { Text("→") }
                }
            }
        }
    }
}
