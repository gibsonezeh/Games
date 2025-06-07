package com.gibson.games.engine

import com.gibson.games.core.SwipeDirection
import com.gibson.games.model.Player

class PlayerController(private val player: Player) {

    private var jumpTime = 0
    private var isSliding = false
    private var slideTime = 0

    fun onSwipe(direction: SwipeDirection) {
        when (direction) {
            SwipeDirection.LEFT -> player.moveToLane(player.lane - 1)
            SwipeDirection.RIGHT -> player.moveToLane(player.lane + 1)
            SwipeDirection.UP -> jump()
            SwipeDirection.DOWN -> slide()
        }
    }

    private fun jump() {
        if (jumpTime == 0) {
            jumpTime = 20 // Arbitrary jump duration (frames)
        }
    }

    private fun slide() {
        if (!isSliding) {
            isSliding = true
            slideTime = 20 // Duration of slide animation (frames)
            // Optional: Shrink player height or change hitbox
        }
    }

    fun update() {
        player.update()

        if (jumpTime > 0) {
            player.position = player.position.copy(y = player.position.y - 12f)
            jumpTime--
        } else if (player.position.y < 1200f) {
            // Bring player back down gradually
            player.position = player.position.copy(y = player.position.y + 12f)
        }

        if (isSliding) {
            slideTime--
            if (slideTime <= 0) {
                isSliding = false
                // Optional: Restore player hitbox size
            }
        }
    }
}
