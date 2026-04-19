package com.gibson.games.ludo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
