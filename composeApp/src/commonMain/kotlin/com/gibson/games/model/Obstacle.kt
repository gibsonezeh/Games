package com.gibson.games.model

import com.gibson.games.core.Vector2

data class Obstacle(
    var position: Vector2,
    val width: Float = 100f,
    val height: Float = 100f,
    val lane: Int = 1 // Lane 0, 1, or 2
)
