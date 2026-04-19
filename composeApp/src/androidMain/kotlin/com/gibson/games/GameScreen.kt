package com.gibson.games

import androidx.compose.runtime.Composable
import com.gibson.games.ludo.LudoNavigationScreen
import com.gibson.games.zuma.ui.ZumaNavigationScreen

@Composable
fun GameScreen(
    game: Game,
    onExit: () -> Unit
) {
    when (game) {
        Game.LUDO -> LudoNavigationScreen(onExit = onExit)
        Game.ZUMA -> ZumaNavigationScreen(onExit = onExit)
    }
}
