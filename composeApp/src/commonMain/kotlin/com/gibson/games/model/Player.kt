package com.gibson.games.model

import com.gibson.games.core.Vector2

class Player(
    override val position: Vector2,
    override val size: Vector2,
    private val speed: Float = 5f,
    private val jumpHeight: Float = 100f,
    private val slideDuration: Long = 500L
) : GameEntity {

    private var isJumping = false
    private var isSliding = false
    private var jumpVelocity = 0f
    private var slideTimer = 0L

    fun moveLeft() {
        position.x -= 50f // Example movement
    }

    fun moveRight() {
        position.x += 50f // Example movement
    }

    fun jump() {
        if (!isJumping) {
            isJumping = true
            jumpVelocity = jumpHeight // Initial upward velocity
        }
    }

    fun slide() {
        if (!isSliding) {
            isSliding = true
            slideTimer = slideDuration
            // Implement visual change for sliding (e.g., change player size/shape)
        }
    }

    override fun update(deltaTime: Long) {
        // Apply gravity if jumping
        if (isJumping) {
            position.y -= jumpVelocity * (deltaTime / 1000f) // Apply jump velocity
            jumpVelocity -= 9.8f * (deltaTime / 1000f) * 50 // Simulate gravity

            if (position.y >= 0) { // Assuming 0 is ground level
                position.y = 0f
                isJumping = false
                jumpVelocity = 0f
            }
        }

        // Handle sliding timer
        if (isSliding) {
            slideTimer -= deltaTime
            if (slideTimer <= 0) {
                isSliding = false
                // Revert visual change for sliding
            }
        }

        // Continuous forward movement
        position.y += speed * (deltaTime / 1000f) // Move player forward along the Y-axis
    }
}

