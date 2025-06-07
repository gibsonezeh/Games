package com.gibson.games.model

import com.gibson.games.core.Vector2

class Player {
    var position = Vector2(500f, 1200f) // Start center-bottom
    var lane = 1 // 0: Left, 1: Middle, 2: Right

    fun moveToLane(laneIndex: Int) {
        lane = laneIndex.coerceIn(0, 2)
        position = Vector2(
            x = 200f + lane * 200f, // Assuming 3 lanes with 200px spacing
            y = position.y
        )
    }

    fun update() {
        // Optional: gravity, jump, or other animations
    }
}
