package com.gibson.games.model

import com.gibson.games.core.Vector2

open class GameEntity(
    open var position: Vector2,
  open  var width: Float,
  open var height: Float
) {
    fun isCollidingWith(other: GameEntity): Boolean {
        return position.x < other.position.x + other.width &&
               position.x + width > other.position.x &&
               position.y < other.position.y + other.height &&
               position.y + height > other.position.y
    }
}
