package com.gibson.games.engine

import androidx.compose.ui.geometry.Size
import com.gibson.games.core.Vector2

data class RoadSegment(val x: Float, val y: Float, val size: Size)

class RoadGenerator {

    private val segments = mutableListOf<RoadSegment>()

    init {
        // Initial road segments
        repeat(10) {
            segments.add(generateSegment(it))
        }
    }

    fun update() {
        // Scroll all segments down
        for (i in segments.indices) {
            val seg = segments[i]
            segments[i] = seg.copy(y = seg.y + 10f)
        }

        // Remove off-screen
        if (segments.isNotEmpty() && segments.first().y > 2000f) {
            segments.removeFirst()
        }

        // Add new at top
        if (segments.isEmpty() || segments.last().y > 0f) {
            segments.add(0, generateSegment(-1))
        }
    }

    fun getRoadSegments(): List<RoadSegment> = segments

    private fun generateSegment(index: Int): RoadSegment {
        return RoadSegment(
            x = 0f,
            y = index * -200f,
            size = Size(width = 720f, height = 200f)
        )
    }
}
