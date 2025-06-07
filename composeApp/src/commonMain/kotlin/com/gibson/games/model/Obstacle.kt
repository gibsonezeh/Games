package com.gibson.games.model

import com.gibson.games.core.Vector2

class Obstacle(
    override var position: Vector2,
    override var width: Float = 100f,
    override var height: Float = 100f,
    val lane: Int = 1 // 0: Left, 1: Middle, 2: Right
) : GameEntity(position, width, height)
