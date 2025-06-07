package com.gibson.games.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Executes the given `onFrame` lambda every frame (~60 FPS).
 */
@Composable
fun GameLoop(
    isRunning: Boolean,
    onFrame: () -> Unit
) {
    LaunchedEffect(isRunning) {
        while (isRunning) {
            onFrame()
            kotlinx.coroutines.delay(16L) // ~60 FPS (1000ms / 60fps = ~16ms)
        }
    }
}
