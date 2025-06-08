package com.gibson.games.ninjaassassin

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class Entity(var x: Float, var y: Float, val size: Float = 40f)

class NinjaAssassinGame {
    val player = Entity(300f, 300f)
    val enemy = Entity(100f, 100f)

    fun update() {
        // Make the enemy chase the player
        val dx = player.x - enemy.x
        val dy = player.y - enemy.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > 1f) {
            val angle = atan2(dy, dx)
            enemy.x += cos(angle) * 2f
            enemy.y += sin(angle) * 2f
        }
    }

    fun movePlayer(dx: Float, dy: Float) {
        player.x += dx
        player.y += dy
    }
}
