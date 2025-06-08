// commonMain/com/gibson/games/ninjaassassin/NinjaAssassinRenderer.kt

package com.gibson.games.ninjaassassin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun NinjaAssassinGameScreen(onExit: () -> Unit) {
    val game = remember { NinjaAssassinGame() }

    LaunchedEffect(Unit) {
        while (true) {
            game.update()
            delay(16L) // 60 FPS
        }
    }

    Column {
        Button(onClick = onExit, modifier = Modifier.padding(8.dp)) {
            Text("Back to Menu")
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Red,
                radius = 20f,
                center = androidx.compose.ui.geometry.Offset(game.player.x, game.player.y)
            )
        }
    }
}
