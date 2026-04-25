package com.gibson.games.tetris.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.gibson.games.tetris.audio.TetrisSound
import com.gibson.games.tetris.audio.TetrisSoundManager
import com.gibson.games.tetris.engine.Direction
import com.gibson.games.tetris.engine.TetrisAction
import com.gibson.games.tetris.engine.TetrisGameEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun TetrisGameScreen(
    onExit: () -> Unit = {}
) {
    val context = LocalContext.current
    val engine = remember { TetrisGameEngine() }
    var state by remember { mutableStateOf(engine.createInitialState()) }

    val soundManager = remember {
        TetrisSoundManager(context)
    }

    LaunchedEffect(state.level, state.isRunning, state.isGameOver) {
        while (isActive && state.isRunning && !state.isGameOver) {
            val speed = (650L - 55L * (state.level - 1))
                .coerceAtLeast(100L)

            delay(speed)

            state = engine.dispatch(
                state = state,
                action = TetrisAction.GameTick
            )
        }
    }

    TetrisGameBody(
        controls = tetrisControls(
            onMove = { direction ->
                state = if (direction == Direction.Up) {
                    soundManager.play(TetrisSound.Drop, state.isMute)
                    engine.dispatch(state, TetrisAction.Drop)
                } else {
                    soundManager.play(TetrisSound.Move, state.isMute)
                    engine.dispatch(state, TetrisAction.Move(direction))
                }
            },
            onRotate = {
                soundManager.play(TetrisSound.Rotate, state.isMute)
                state = engine.dispatch(state, TetrisAction.Rotate)
            },
            onRestart = {
                soundManager.play(TetrisSound.Start, state.isMute)
                state = engine.dispatch(state, TetrisAction.Reset)
            },
            onPause = {
                state = if (state.isRunning) {
                    engine.dispatch(state, TetrisAction.Pause)
                } else {
                    engine.dispatch(state, TetrisAction.Resume)
                }
            },
            onMute = {
                state = engine.dispatch(state, TetrisAction.Mute)
            }
        )
    ) {
        TetrisGameScreenContent(
            state = state,
            ghostPiece = engine.getGhostPiece(state)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }
}
