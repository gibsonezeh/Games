package com.gibson.games.engine

import com.gibson.games.core.SwipeDirection
import com.gibson.games.model.Player

class PlayerController(private val player: Player) {

    fun onSwipe(direction: SwipeDirection) {
        when (direction) {
            SwipeDirection.LEFT -> player.moveToLane(player.lane - 1)
            SwipeDirection.RIGHT -> player.moveToLane(player.lane + 1)
            SwipeDirection.UP -> jump()
            SwipeDirection.DOWN -> slide()
        }
    }

    private var jumpTime = 0
    private var isSliding = false

    private fun jump() {
        jumpTime = 20 // Arbitrary jump duration
    }

    private fun slide() {
        isSliding = true
        // Reset after short duration or implement timer
    }

    fun update() {
        player.update()

        if (jumpTime > 0) {
            player.position = player.position.copy(y = player.position.y - 10f)
            jumpTime--
        } else {
            // Fall back to default Y
            player.position = player.position.copy(y = 1200f)
        }
    }
}
