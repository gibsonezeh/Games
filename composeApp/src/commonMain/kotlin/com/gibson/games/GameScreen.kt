// commonMain/com/gibson/games/GameScreen.kt

package com.gibson.games

import androidx.compose.runtime.Composable
import com.gibson.games.ninjaassassin.NinjaAssassinGameScreen

@Composable
fun GameScreen(game: Game, onExit: () -> Unit) {
    when (game) {
        Game.NinjaAssassin -> NinjaAssassinGameScreen(onExit = onExit)
        // Add other games later
    }
}
