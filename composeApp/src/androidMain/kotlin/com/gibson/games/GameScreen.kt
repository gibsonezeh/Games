package com.gibson.games

import androidx.compose.runtime.Composable
import com.gibson.games.ludo.LudoNavigationScreen
import com.gibson.games.zuma.ui.ZumaNavigationScreen
import com.gibson.games.carrom.ui.CarromGameScreen // ✅ add this import

@Composable
fun GameScreen(
    game: Game,
    onExit: () -> Unit
) {
    when (game) {
        Game.LUDO -> LudoNavigationScreen(onExit = onExit)

        Game.ZUMA -> ZumaNavigationScreen(onExit = onExit)

        Game.CARROM -> CarromGameScreen( // ✅ add this block
            onExit = onExit
        )
    }
}
