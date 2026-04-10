package com.gibson.games.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        modifier = Modifier.Companion.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.Companion.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.Companion.weight(1f)
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