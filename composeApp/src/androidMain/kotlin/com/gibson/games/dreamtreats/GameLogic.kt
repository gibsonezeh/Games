package com.gibson.games.dreamtreats

import kotlin.random.Random

// Note: TreatType and GameTile are now defined in HarmonizedGameTile.kt

object GameLogic {
    const val BOARD_SIZE = 6 // Keep board size consistent

    fun createBoard(): List<List<GameTile>> {
        // Ensure no matches on creation (simple approach: retry until no matches)
        var board: List<List<GameTile>>
        do {
            board = List(BOARD_SIZE) { y ->
                List(BOARD_SIZE) { x ->
                    GameTile(x, y, TreatType.values().random())
                }
            }
        } while (detectMatches(board).isNotEmpty())
        return board
    }

    // Using only cardinal directions for match detection as per typical match-3
    private val matchDirections = listOf(
        Pair(0, 1),   // Down
        Pair(1, 0)    // Right
    )

    // Simplified match detection for horizontal and vertical lines of 3+
    fun detectMatches(board: List<List<GameTile>>): Set<GameTile> {
        val matchedTiles = mutableSetOf<GameTile>()

        // Check horizontal matches
        for (y in 0 until BOARD_SIZE) {
            for (x in 0 until BOARD_SIZE - 2) {
                val tile1 = board[y][x]
                val tile2 = board[y][x + 1]
                val tile3 = board[y][x + 2]
                if (tile1.type == tile2.type && tile2.type == tile3.type) {
                    matchedTiles.add(tile1)
                    matchedTiles.add(tile2)
                    matchedTiles.add(tile3)
                    // Check for longer matches
                    for (k in x + 3 until BOARD_SIZE) {
                        if (board[y][k].type == tile1.type) {
                            matchedTiles.add(board[y][k])
                        } else {
                            break
                        }
                    }
                }
            }
        }

        // Check vertical matches
        for (x in 0 until BOARD_SIZE) {
            for (y in 0 until BOARD_SIZE - 2) {
                val tile1 = board[y][x]
                val tile2 = board[y + 1][x]
                val tile3 = board[y + 2][x]
                if (tile1.type == tile2.type && tile2.type == tile3.type) {
                    matchedTiles.add(tile1)
                    matchedTiles.add(tile2)
                    matchedTiles.add(tile3)
                    // Check for longer matches
                    for (k in y + 3 until BOARD_SIZE) {
                        if (board[k][x].type == tile1.type) {
                            matchedTiles.add(board[k][x])
                        } else {
                            break
                        }
                    }
                }
            }
        }

        return matchedTiles
    }

    // Function to check if a swap results in a match
    fun isSwapValid(board: List<List<GameTile>>, x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        if (x1 !in 0 until BOARD_SIZE || y1 !in 0 until BOARD_SIZE ||
            x2 !in 0 until BOARD_SIZE || y2 !in 0 until BOARD_SIZE) {
            return false // Out of bounds
        }

        // Check if adjacent
        if (abs(x1 - x2) + abs(y1 - y2) != 1) {
            return false // Not adjacent
        }

        // Create a temporary board with the swap
        val tempBoard = board.map { it.toMutableList() }.toMutableList()
        val tempTile = tempBoard[y1][x1]
        tempBoard[y1][x1] = tempBoard[y2][x2].copy(x = x1, y = y1)
        tempBoard[y2][x2] = tempTile.copy(x = x2, y = y2)

        // Check if the swap creates a match involving either swapped tile
        return detectMatches(tempBoard).any { it.id == tempBoard[y1][x1].id || it.id == tempBoard[y2][x2].id }
    }

    fun clearMatchesAndRefill(board: List<List<GameTile>>, matches: Set<GameTile>): List<List<GameTile>> {
        val mutableBoard = board.map { it.toMutableList() }.toMutableList()
        val matchedCoords = matches.map { it.x to it.y }.toSet()

        // Mark matched tiles (e.g., set type to null or use a flag)
        matches.forEach { tile ->
            mutableBoard[tile.y][tile.x] = mutableBoard[tile.y][tile.x].copy(isMatched = true)
        }

        // Apply gravity column by column
        for (x in 0 until BOARD_SIZE) {
            val column = mutableListOf<GameTile?>()
            // Add non-matched tiles from bottom up
            for (y in BOARD_SIZE - 1 downTo 0) {
                if (!mutableBoard[y][x].isMatched) {
                    column.add(mutableBoard[y][x])
                }
            }

            // Fill the rest with nulls (representing empty spaces)
            while (column.size < BOARD_SIZE) {
                column.add(null)
            }

            // Place tiles back into the column, from bottom up
            for (y in BOARD_SIZE - 1 downTo 0) {
                mutableBoard[y][x] = column[BOARD_SIZE - 1 - y]?.copy(y = y) ?: 
                                     // Generate new tile for empty space
                                     GameTile(x, y, TreatType.values().random())
            }
        }
        
        // Reset isMatched flag for the new board state
        val finalBoard = mutableBoard.map { row -> 
            row.map { tile -> tile.copy(isMatched = false) } 
        }

        return finalBoard
    }
}

