package com.gibson.games

import androidx.compose.ui.graphics.Color

// Represents the 15x15 grid structure of a Ludo board
const val GRID_SIZE = 15
const val TOTAL_CELLS_ON_TRACK = 52
const val CELLS_IN_HOME_PATH = 6

// Define the 52-cell main track path (0-indexed)
// (row, col) for a 15x15 grid. Player start positions are relative to this path.
val mainTrackPath: List<Pair<Int, Int>> = listOf(
    // Red"s arm leading out (index 0 is Red"s first step *outside* the yard)
    Pair(6, 1), Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5), // 0-4 (Red"s arm)
    Pair(5, 6), Pair(4, 6), Pair(3, 6), Pair(2, 6), Pair(1, 6), // 5-9 (Upwards towards Green"s yard)
    Pair(0, 6), // 10 (Top-left corner before Green"s arm)
    Pair(0, 7), // 11 (Cell before Green"s arm)
    Pair(0, 8), // 12 (Top-right corner after Green"s arm)
    Pair(1, 8), Pair(2, 8), Pair(3, 8), Pair(4, 8), Pair(5, 8), // 13-17 (Green"s arm)
    Pair(6, 9), Pair(6, 10), Pair(6, 11), Pair(6, 12), Pair(6, 13), // 18-22 (Rightwards towards Yellow"s yard)
    Pair(6, 14), // 23 (Mid-right corner before Yellow"s arm)
    Pair(7, 14), // 24 (Cell before Yellow"s arm)
    Pair(8, 14), // 25 (Bottom-right corner after Yellow"s arm)
    Pair(8, 13), Pair(8, 12), Pair(8, 11), Pair(8, 10), Pair(8, 9), // 26-30 (Yellow"s arm)
    Pair(9, 8), Pair(10, 8), Pair(11, 8), Pair(12, 8), Pair(13, 8), // 31-35 (Downwards towards Blue"s yard)
    Pair(14, 8), // 36 (Bottom-right corner before Blue"s arm)
    Pair(14, 7), // 37 (Cell before Blue"s arm)
    Pair(14, 6), // 38 (Bottom-left corner after Blue"s arm)
    Pair(13, 6), Pair(12, 6), Pair(11, 6), Pair(10, 6), Pair(9, 6), // 39-43 (Blue"s arm)
    Pair(8, 5), Pair(8, 4), Pair(8, 3), Pair(8, 2), Pair(8, 1), // 44-48 (Leftwards towards Red"s yard)
    Pair(8, 0), // 49 (Mid-left corner before Red"s home path)
    Pair(7, 0), // 50 (Cell before Red"s home path)
    Pair(6, 0)  // 51 (Cell that is Red"s home entry point, just before its home path starts)
)

// Player-specific home path coordinates (6 cells each, leading to center)
// (row, col) for a 15x15 grid. Index 0 is first step into home path, index 5 is last step before center.
val playerHomePaths: Map<String, List<Pair<Int, Int>>> = mapOf(
    "red"    to listOf(Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5), Pair(7, 6)),
    "green"  to listOf(Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7), Pair(6, 7)),
    "yellow" to listOf(Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9), Pair(7, 8)),
    "blue"   to listOf(Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7), Pair(8, 7))
)

// Player start cells on the mainTrackPath (0-indexed)
// This is the index on mainTrackPath where the piece lands when exiting the yard.
val playerStartTrackIndices: Map<String, Int> = mapOf(
    "red"    to 0,
    "green"  to 13,
    "yellow" to 26,
    "blue"   to 39
)

// Player home entry cells on the mainTrackPath (0-indexed)
// This is the index on mainTrackPath *just before* the piece would turn into its home path.
val playerHomeEntryTrackIndices: Map<String, Int> = mapOf(
    "red"    to 51,
    "green"  to 12,
    "yellow" to 25,
    "blue"   to 38
)

// Global safe zone track indices (0-indexed on mainTrackPath)
val globalSafeTrackIndices: List<Int> = listOf(
    playerStartTrackIndices["red"]!!,
    playerStartTrackIndices["green"]!!,
    playerStartTrackIndices["yellow"]!!,
    playerStartTrackIndices["blue"]!!,
    mainTrackPath.indexOf(Pair(0,7)), // Green"s arm top-center (index 11)
    mainTrackPath.indexOf(Pair(7,14)), // Yellow"s arm mid-right (index 24)
    mainTrackPath.indexOf(Pair(14,7)), // Blue"s arm bottom-center (index 37)
    mainTrackPath.indexOf(Pair(7,0))  // Red"s arm mid-left (index 50)
).distinct().filter { it != -1 } // Filter out -1 in case indexOf fails, though it shouldn"t with this path

object PlayerColors {
    val RED = Color(0xFFD32F2F)
    val GREEN = Color(0xFF388E3C)
    val YELLOW = Color(0xFFFBC02D)
    val BLUE = Color(0xFF1976D2)
    val DEFAULT = Color.Gray
}

