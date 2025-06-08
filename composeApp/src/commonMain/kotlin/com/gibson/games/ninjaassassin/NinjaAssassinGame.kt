// commonMain/com/gibson/games/ninjaassassin/NinjaAssassinGame.kt

package com.gibson.games.ninjaassassin

data class NinjaPlayer(var x: Float = 100f, var y: Float = 100f)

class NinjaAssassinGame {
    var player = NinjaPlayer()

    fun update() {
        // Placeholder for movement, enemy logic, collisions
        player.x += 1f // Test movement
    }
}
