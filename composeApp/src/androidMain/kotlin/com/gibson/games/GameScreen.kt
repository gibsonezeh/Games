package com.gibson.games

import androidx.compose.runtime.Composable
import com.gibson.games.ludo.LudoNavigationScreen
import com.gibson.games.zuma.ui.ZumaNavigationScreen
import com.gibson.games.carrom.ui.CarromGameScreen
import com.gibson.games.tetris.ui.TetrisGameScreen // ✅ ADD THIS

@Composable
fun GameScreen(
    game: Game,
    onExit: () -> Unit
) {
    when (game) {
        Game.LUDO -> LudoNavigationScreen(onExit = onExit)

        Game.ZUMA -> ZumaNavigationScreen(onExit = onExit)

        Game.CARROM -> CarromGameScreen(
            onExit = onExit
        )

        // ✅ ADD THIS BLOCK
        Game.TETRIS -> TetrisGameScreen(
            onExit = onExit
        )
    }
}
