package com.gibson.games.model

import com.gibson.games.core.Vector2

class Obstacle(
    override val position: Vector2,
    override val size: Vector2
) : GameEntity {
    override fun update(deltaTime: Long) {
        // Obstacles typically don't update their own position, they are static relative to the road
        // Their position is managed by the RoadGenerator
    }
}

