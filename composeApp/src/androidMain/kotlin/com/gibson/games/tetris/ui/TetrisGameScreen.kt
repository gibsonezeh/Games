package com.gibson.games.tetris.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gibson.games.tetris.engine.Direction
import com.gibson.games.tetris.engine.TetrisAction
import com.gibson.games.tetris.engine.TetrisGameEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun TetrisGameScreen(
    onExit: () -> Unit = {}
) {
    val engine = remember { TetrisGameEngine() }
    var state by remember { mutableStateOf(engine.createInitialState()) }

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
                    engine.dispatch(state, TetrisAction.Drop)
                } else {
                    engine.dispatch(state, TetrisAction.Move(direction))
                }
            },
            onRotate = {
                state = engine.dispatch(state, TetrisAction.Rotate)
            },
            onRestart = {
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
        TetrisGameScreenContent(state = state)
    }
}
