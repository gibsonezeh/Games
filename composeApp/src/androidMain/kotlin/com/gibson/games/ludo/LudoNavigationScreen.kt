package com.gibson.games.ludo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private enum class LudoScreen {
    MODE_MENU,
    SETUP,
    GAME,
    SETTINGS
}

@Composable
fun LudoNavigationScreen(
    onExit: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(LudoScreen.MODE_MENU) }
    var gameRules by remember { mutableStateOf(GameRules()) }
    var selectedMode by remember { mutableStateOf(LudoMode.QUICK_PLAY) }
    var setupConfig by remember {
        mutableStateOf(
            LudoSetupConfig(
                mode = LudoMode.QUICK_PLAY,
                playerCount = 1,
                playerNames = listOf("Player 1")
            )
        )
    }

    when (currentScreen) {
        LudoScreen.MODE_MENU -> {
            LudoModeScreen(
                onBackClicked = onExit,
                onModeSelected = { mode ->
                    selectedMode = mode
                    currentScreen = when (mode) {
                        LudoMode.QUICK_PLAY,
                        LudoMode.PLAY_VS_AI,
                        LudoMode.PASS_AND_PLAY,
                        LudoMode.BLUETOOTH,
                        LudoMode.WIFI,
                        LudoMode.ONLINE -> LudoScreen.SETUP
                    }
                }
            )
        }

        LudoScreen.SETUP -> {
            LudoSetupScreen(
                mode = selectedMode,
                onBackClicked = {
                    currentScreen = LudoScreen.MODE_MENU
                },
                onStartGame = { config ->
                    setupConfig = config
                    currentScreen = LudoScreen.GAME
                }
            )
        }

        LudoScreen.GAME -> {
            LudoGameScreen(
                onExit = {
                    currentScreen = LudoScreen.MODE_MENU
                },
                gameRules = gameRules
            )
        }

        LudoScreen.SETTINGS -> {
            LudoSettingsScreen(
                onBackClicked = {
                    currentScreen = LudoScreen.MODE_MENU
                },
                gameRules = gameRules,
                onRulesChanged = { updatedRules ->
                    gameRules = updatedRules
                }
            )
        }
    }
}
