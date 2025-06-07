package com.gibson.games.engine

import com.gibson.games.core.Vector2
import com.gibson.games.model.Obstacle
import kotlin.random.Random

class ObstacleManager {

    private val obstacles = mutableListOf<Obstacle>()
    private var spawnTimer = 0
    private val spawnInterval = 60 // Frames (e.g., spawn every 60 frames)

    fun update() {
        // Move all obstacles downward
        obstacles.forEach { it.position = it.position.copy(y = it.position.y + 10f) }

        // Remove off-screen obstacles
        obstacles.removeAll { it.position.y > 2000f }

        // Handle spawn timing
        spawnTimer++
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0
            spawnObstacle()
        }
    }

    private fun spawnObstacle() {
        val lane = Random.nextInt(0, 3)
        val x = 200f + lane * 200f
        val y = -100f // Start above the screen
        obstacles.add(Obstacle(position = Vector2(x, y), lane = lane))
    }

    fun getObstacles(): List<Obstacle> = obstacles
}
