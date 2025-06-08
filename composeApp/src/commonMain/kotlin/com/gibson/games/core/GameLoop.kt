package com.gibson.games.core

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameLoop(private val scope: CoroutineScope) {

    private var gameJob: Job? = null
    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    fun startGame(updateIntervalMs: Long = 16L, onUpdate: (Long) -> Unit) {
        if (gameJob?.isActive == true) return

        _isRunning.value = true
        gameJob = scope.launch {
            var lastFrameTime = System.nanoTime()
            while (_isRunning.value) {
                val currentTime = System.nanoTime()
                val deltaTime = (currentTime - lastFrameTime) / 1_000_000L // Convert to milliseconds
                lastFrameTime = currentTime

                onUpdate(deltaTime)
                delay(updateIntervalMs)
            }
        }
    }

    fun stopGame() {
        _isRunning.value = false
        gameJob?.cancel()
        gameJob = null
    }
}

