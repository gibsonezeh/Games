package com.gibson.games.ludo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gibson.games.multiplayer.MultiplayerConnectionType
import com.gibson.games.multiplayer.MultiplayerLobbyScreen
import com.gibson.games.multiplayer.MultiplayerSession

private enum class LudoScreen {
    MODE_MENU,
    SETUP,
    MULTIPLAYER,
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
                        LudoMode.BLUETOOTH,
                        LudoMode.WIFI,
                        LudoMode.ONLINE -> LudoScreen.MULTIPLAYER

                        LudoMode.QUICK_PLAY,
                        LudoMode.PLAY_VS_AI,
                        LudoMode.PASS_AND_PLAY -> LudoScreen.SETUP
                    }
                }
            )
        }

        LudoScreen.MULTIPLAYER -> {
            MultiplayerLobbyScreen(
                onBackClicked = {
                    MultiplayerSession.clear()
                    currentScreen = LudoScreen.MODE_MENU
                },
                onConnected = {
                    val connectedMode = when (MultiplayerSession.connectionType) {
                        MultiplayerConnectionType.BLUETOOTH -> LudoMode.BLUETOOTH
                        MultiplayerConnectionType.WIFI -> LudoMode.WIFI
                        MultiplayerConnectionType.ONLINE -> LudoMode.ONLINE
                        MultiplayerConnectionType.NONE -> selectedMode
                    }

                    setupConfig = LudoSetupConfig(
                        mode = connectedMode,
                        playerCount = 2,
                        playerNames = listOf(
                            MultiplayerSession.localDisplayName,
                            MultiplayerSession.remoteDisplayName
                        )
                    )

                    currentScreen = LudoScreen.GAME
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
                gameRules = gameRules,
                setupConfig = setupConfig
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
