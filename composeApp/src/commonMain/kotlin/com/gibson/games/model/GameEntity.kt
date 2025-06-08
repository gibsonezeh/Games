package com.gibson.games.model

import com.gibson.games.core.Vector2

interface GameEntity {
    val position: Vector2
    val size: Vector2

    fun update(deltaTime: Long)
}

