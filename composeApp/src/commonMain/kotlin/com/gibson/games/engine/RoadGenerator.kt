package com.gibson.games.engine

import com.gibson.games.model.Obstacle
import com.gibson.games.core.Vector2
import kotlin.random.Random

class RoadGenerator {

    private val obstacles = mutableListOf<Obstacle>()
    private var lastObstacleZ = 0f

    fun generateInitialRoad(initialObstacleCount: Int, roadLength: Float) {
        for (i in 0 until initialObstacleCount) {
            generateNewObstacle(roadLength / initialObstacleCount * i)
        }
    }

    fun update(playerZ: Float, roadLength: Float) {
        // Remove obstacles that are behind the player
        obstacles.removeAll { it.position.y < playerZ - 500 }

        // Generate new obstacles as the player moves forward
        while (lastObstacleZ < playerZ + roadLength) {
            generateNewObstacle(lastObstacleZ + Random.nextFloat() * 200 + 100) // Random spacing
        }
    }

    private fun generateNewObstacle(zPos: Float) {
        val lane = Random.nextInt(3) // 0 for left, 1 for middle, 2 for right
        val xPos = when (lane) {
            0 -> -100f // Example x for left lane
            1 -> 0f     // Example x for middle lane
            2 -> 100f   // Example x for right lane
            else -> 0f
        }
        val obstacle = Obstacle(Vector2(xPos, zPos), Vector2(50f, 50f)) // Example size
        obstacles.add(obstacle)
        lastObstacleZ = zPos
    }

    fun getObstacles(): List<Obstacle> = obstacles
}

