package com.gibson.games.core

data class Vector2(var x: Float, var y: Float) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vector2(x / scalar, y / scalar)

    fun length() = kotlin.math.sqrt(x * x + y * y)
    fun normalize() = this / length()
}

