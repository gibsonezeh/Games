package com.gibson.games.core

data class Vector2(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun add(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun reset() {
        x = 0f
        y = 0f
    }

    fun length(): Float {
        return kotlin.math.sqrt(x * x + y * y)
    }

    fun normalize(): Vector2 {
        val len = length()
        return if (len != 0f) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }

    fun clone(): Vector2 {
        return Vector2(x, y)
    }
}
