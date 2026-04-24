package com.gibson.games.carrom

import kotlin.math.*

class CarromGameEngine {

    private val friction = 0.985f
    private val minSpeed = 0.08f

    fun createInitialState(): CarromState {
        val centerX = 500f
        val centerY = 500f
        val r = 20f

        val discs = mutableListOf<CarromDisc>()

        // Queen
        discs += CarromDisc(
            id = 0,
            type = DiscType.QUEEN,
            x = centerX,
            y = centerY,
            radius = r
        )

        // Simple starting cluster
        var id = 1
        val positions = listOf(
            -40f to 0f,
            40f to 0f,
            0f to -40f,
            0f to 40f,
            -30f to -30f,
            30f to -30f,
            -30f to 30f,
            30f to 30f,
            -70f to 0f,
            70f to 0f
        )

        positions.forEachIndexed { index, offset ->
            discs += CarromDisc(
                id = id++,
                type = if (index % 2 == 0) DiscType.BLACK else DiscType.WHITE,
                x = centerX + offset.first,
                y = centerY + offset.second,
                radius = r
            )
        }

        // Striker
        discs += CarromDisc(
            id = 99,
            type = DiscType.STRIKER,
            x = centerX,
            y = 850f,
            radius = 24f
        )

        return CarromState(discs = discs)
    }

    fun shootStriker(
        state: CarromState,
        aimX: Float,
        aimY: Float,
        power: Float
    ): CarromState {
        val discs = state.discs.map { disc ->
            if (disc.type == DiscType.STRIKER && !disc.pocketed) {
                val dx = aimX - disc.x
                val dy = aimY - disc.y
                val length = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

                val dirX = dx / length
                val dirY = dy / length

                disc.copy(
                    vx = dirX * power,
                    vy = dirY * power
                )
            } else {
                disc
            }
        }

        return state.copy(
            discs = discs,
            isMoving = true,
            pocketedThisTurn = emptyList()
        )
    }

    fun update(state: CarromState): CarromState {
        var discs = state.discs

        discs = moveDiscs(discs, state.boardWidth, state.boardHeight)
        discs = resolveDiscCollisions(discs)
        val pocketResult = handlePockets(discs, state)

        val stillMoving = pocketResult.discs.any {
            !it.pocketed && speed(it) > minSpeed
        }

        return state.copy(
            discs = pocketResult.discs,
            isMoving = stillMoving,
            pocketedThisTurn = state.pocketedThisTurn + pocketResult.pocketed
        )
    }

    private fun moveDiscs(
        discs: List<CarromDisc>,
        boardWidth: Float,
        boardHeight: Float
    ): List<CarromDisc> {
        return discs.map { disc ->
            if (disc.pocketed) return@map disc

            var newX = disc.x + disc.vx
            var newY = disc.y + disc.vy
            var newVx = disc.vx * friction
            var newVy = disc.vy * friction

            if (abs(newVx) < minSpeed) newVx = 0f
            if (abs(newVy) < minSpeed) newVy = 0f

            // Wall bounce
            if (newX - disc.radius < 0f) {
                newX = disc.radius
                newVx *= -1
            }

            if (newX + disc.radius > boardWidth) {
                newX = boardWidth - disc.radius
                newVx *= -1
            }

            if (newY - disc.radius < 0f) {
                newY = disc.radius
                newVy *= -1
            }

            if (newY + disc.radius > boardHeight) {
                newY = boardHeight - disc.radius
                newVy *= -1
            }

            disc.copy(
                x = newX,
                y = newY,
                vx = newVx,
                vy = newVy
            )
        }
    }

    private fun resolveDiscCollisions(discs: List<CarromDisc>): List<CarromDisc> {
        val result = discs.toMutableList()

        for (i in result.indices) {
            for (j in i + 1 until result.size) {
                val a = result[i]
                val b = result[j]

                if (a.pocketed || b.pocketed) continue

                val dx = b.x - a.x
                val dy = b.y - a.y
                val distance = sqrt(dx * dx + dy * dy)
                val minDistance = a.radius + b.radius

                if (distance > 0f && distance < minDistance) {
                    val nx = dx / distance
                    val ny = dy / distance

                    val overlap = minDistance - distance

                    val ax = a.x - nx * overlap / 2f
                    val ay = a.y - ny * overlap / 2f
                    val bx = b.x + nx * overlap / 2f
                    val by = b.y + ny * overlap / 2f

                    val dvx = b.vx - a.vx
                    val dvy = b.vy - a.vy
                    val impact = dvx * nx + dvy * ny

                    if (impact < 0f) {
                        val impulse = impact

                        result[i] = a.copy(
                            x = ax,
                            y = ay,
                            vx = a.vx + impulse * nx,
                            vy = a.vy + impulse * ny
                        )

                        result[j] = b.copy(
                            x = bx,
                            y = by,
                            vx = b.vx - impulse * nx,
                            vy = b.vy - impulse * ny
                        )
                    }
                }
            }
        }

        return result
    }

    private fun handlePockets(
        discs: List<CarromDisc>,
        state: CarromState
    ): PocketResult {
        val pockets = listOf(
            Pocket(0f, 0f),
            Pocket(state.boardWidth, 0f),
            Pocket(0f, state.boardHeight),
            Pocket(state.boardWidth, state.boardHeight)
        )

        val pocketed = mutableListOf<CarromDisc>()

        val updated = discs.map { disc ->
            if (disc.pocketed) return@map disc

            val insidePocket = pockets.any { pocket ->
                val dx = disc.x - pocket.x
                val dy = disc.y - pocket.y
                sqrt(dx * dx + dy * dy) < pocket.radius
            }

            if (insidePocket) {
                pocketed += disc

                disc.copy(
                    pocketed = true,
                    vx = 0f,
                    vy = 0f
                )
            } else {
                disc
            }
        }

        return PocketResult(updated, pocketed)
    }

    private fun speed(disc: CarromDisc): Float {
        return sqrt(disc.vx * disc.vx + disc.vy * disc.vy)
    }

    private data class PocketResult(
        val discs: List<CarromDisc>,
        val pocketed: List<CarromDisc>
    )
}
