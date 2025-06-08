package com.gibson.games.engine

import com.gibson.games.core.SwipeDirection
import com.gibson.games.model.Player

class PlayerController(private val player: Player) {

    fun handleSwipe(direction: SwipeDirection) {
        when (direction) {
            SwipeDirection.LEFT -> player.moveLeft()
            SwipeDirection.RIGHT -> player.moveRight()
            SwipeDirection.UP -> player.jump()
            SwipeDirection.DOWN -> player.slide()
            SwipeDirection.NONE -> { /* Do nothing */ }
        }
    }

    fun update(deltaTime: Long) {
        player.update(deltaTime)
    }
}

