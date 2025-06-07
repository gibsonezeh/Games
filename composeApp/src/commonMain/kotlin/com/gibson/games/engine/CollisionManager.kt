package com.gibson.games.engine

import com.gibson.games.model.Obstacle
import com.gibson.games.model.Player

class CollisionManager {

    fun checkCollisions(player: Player, obstacles: List<Obstacle>): Boolean {
        for (obstacle in obstacles) {
            if (isColliding(player, obstacle)) {
                return true
            }
        }
        return false
    }

    private fun isColliding(player: Player, obstacle: Obstacle): Boolean {
        val px = player.position.x
        val py = player.position.y
        val pw = 80f
        val ph = 80f

        val ox = obstacle.position.x
        val oy = obstacle.position.y
        val ow = obstacle.width
        val oh = obstacle.height

        val intersectsHorizontally = px < ox + ow && px + pw > ox
        val intersectsVertically = py < oy + oh && py + ph > oy

        return intersectsHorizontally && intersectsVertically
    }
}
