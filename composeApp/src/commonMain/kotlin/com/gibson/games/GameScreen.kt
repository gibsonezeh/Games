package com.gibson.games

import androidx.compose.runtime.*
import com.gibson.games.ludo.GameRules
import com.gibson.games.ludo.LudoGameScreen
import com.gibson.games.ludo.LudoMainMenuScreen
import com.gibson.games.ludo.LudoSettingsScreen

/**
 * A wrapper screen for the selected game.
 * Each game manages its own internal navigation and back behavior.
 */
@Composable
fun GameScreen(
    game: Game,
    onExit: () -> Unit
) {
    when (game) {
        Game.LUDO -> LudoNavigationScreen(onExit = onExit)
    }
}

/**
 * Internal navigation routes for Ludo.
 */
private enum class LudoScreen {
    MAIN_MENU,
    GAME,
    SETTINGS
}

/**
 * Navigation controller for Ludo screens.
 *
 * Owns the shared Ludo state that must survive screen switches,
 * especially the selected game rules.
 */
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
                onExitClicked = {
                    onExit()
                },
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
