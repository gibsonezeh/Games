package com.gibson.games.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.gibson.games.Game
import com.gibson.games.GameMenu
import com.gibson.games.GameScreen

@Composable
fun MultiGameApp(
    bottomContent: @Composable () -> Unit = {}
) {
    var selectedGame by remember { mutableStateOf<Game?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                selectedGame?.let { game ->
                    GameScreen(
                        game = game,
                        onExit = {
                            selectedGame = null
                        }
                    )
                } ?: GameMenu(
                    onGameSelected = { game ->
                        selectedGame = game
                    }
                )
            }

            bottomContent()
        }
    }
}
