package com.gibson.games

import androidx.compose.runtime.*
import com.gibson.games.ludo.GameRules
import com.gibson.games.ludo.LudoGameScreen
import com.gibson.games.ludo.LudoMainMenuScreen
import com.gibson.games.ludo.LudoSettingsScreen
import com.gibson.games.Game

@Composable
fun GameScreen(
    game: Game,
    onExit: () -> Unit
) {
    when (game) {
        Game.LUDO -> LudoNavigationScreen(onExit = onExit)
    }
}

private enum class LudoScreen {
    MAIN_MENU,
    GAME,
    SETTINGS
}

@Composable
fun LudoNavigationScreen(
    onExit: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(LudoScreen.MAIN_MENU) }
    var gameRules by remember { mutableStateOf(GameRules()) }

    when (currentScreen) {
        LudoScreen.MAIN_MENU -> {
            LudoMainMenuScreen(
                onPlayClicked = {
                    currentScreen = LudoScreen.GAME
                },
                onExitClicked = onExit,
                onSettingsClicked = {
                    currentScreen = LudoScreen.SETTINGS
                }
            )
        }

        LudoScreen.GAME -> {
            LudoGameScreen(
                onExit = {
                    currentScreen = LudoScreen.MAIN_MENU
                },
                gameRules = gameRules
            )
        }

        LudoScreen.SETTINGS -> {
            LudoSettingsScreen(
                onBackClicked = {
                    currentScreen = LudoScreen.MAIN_MENU
                },
                gameRules = gameRules,
                onRulesChanged = { updatedRules ->
                    gameRules = updatedRules
                }
            )
        }
    }
}
