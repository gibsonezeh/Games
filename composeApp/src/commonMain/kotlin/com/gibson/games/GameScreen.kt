// commonMain/com/gibson/games/GameScreen.kt

package com.gibson.games

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import com.gibson.games.ludo.LudoGameScreen

@Composable
fun GameScreen(game: Game, onExit: () -> Unit) {

    BackHandler {
        onExit()
    }

    when (game) {
        Game.Ludo -> LudoGameScreen(onExit = onExit)
        // Add other games later
    }
}
