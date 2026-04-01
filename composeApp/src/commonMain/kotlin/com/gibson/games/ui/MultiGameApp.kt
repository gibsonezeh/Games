package com.gibson.games.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import com.gibson.games.*

@Composable
fun MultiGameApp(
    bottomContent: @Composable () -> Unit = {}
) {
    var selectedGame by remember { mutableStateOf<Game?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                if (selectedGame == null) {
                    GameMenu(onGameSelected = { selectedGame = it })
                } else {
                    GameScreen(game = selectedGame!!) {
                        selectedGame = null
                    }
                }
            }

            bottomContent()
        }
    }
}
