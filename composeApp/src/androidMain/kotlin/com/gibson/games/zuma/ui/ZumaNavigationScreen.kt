package com.gibson.games.zuma.ui

import androidx.compose.runtime.*
import com.gibson.games.zuma.ZumaGameEngine

private enum class ZumaScreen {
    GAME
}

@Composable
fun ZumaNavigationScreen(
    onExit: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(ZumaScreen.GAME) }

    when (currentScreen) {
        ZumaScreen.GAME -> {
            ZumaGameScreen(
                onExit = onExit
            )
        }
    }
}
