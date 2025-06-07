package com.gibson.games.gamehub.subway

import com.gibson.games.engine.CollisionManager
import com.gibson.games.engine.ObstacleManager
import com.gibson.games.engine.PlayerController
import com.gibson.games.model.Player
import kotlinx.coroutines.delay

class SubwayLogic(
    private val player: Player,
    private val playerController: PlayerController,
    private val obstacleManager: ObstacleManager,
    private val collisionManager: CollisionManager
) {
    var isGameOver = false
        private set

    suspend fun startGameLoop(onGameOver: () -> Unit) {
        while (!isGameOver) {
            updateGame()
            delay(16L) // ~60 FPS
        }
        onGameOver()
    }

    private fun updateGame() {
        playerController.update()
        obstacleManager.update()
        isGameOver = collisionManager.checkCollisions(player, obstacleManager.getObstacles())
    }
}
