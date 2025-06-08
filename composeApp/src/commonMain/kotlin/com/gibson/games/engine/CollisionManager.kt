package com.gibson.games.engine

import com.gibson.games.model.Player
import com.gibson.games.model.Obstacle

object CollisionManager {

    fun checkCollision(player: Player, obstacles: List<Obstacle>): Boolean {
        for (obstacle in obstacles) {
            if (isColliding(player, obstacle)) {
                return true
            }
        }
        return false
    }

    private fun isColliding(player: Player, obstacle: Obstacle): Boolean {
        // Simple AABB collision detection for demonstration
        val playerLeft = player.position.x - player.size.x / 2
        val playerRight = player.position.x + player.size.x / 2
        val playerTop = player.position.y - player.size.y / 2
        val playerBottom = player.position.y + player.size.y / 2

        val obstacleLeft = obstacle.position.x - obstacle.size.x / 2
        val obstacleRight = obstacle.position.x + obstacle.size.x / 2
        val obstacleTop = obstacle.position.y - obstacle.size.y / 2
        val obstacleBottom = obstacle.position.y + obstacle.size.y / 2

        return playerLeft < obstacleRight &&
               playerRight > obstacleLeft &&
               playerTop < obstacleBottom &&
               playerBottom > obstacleTop
    }
}

