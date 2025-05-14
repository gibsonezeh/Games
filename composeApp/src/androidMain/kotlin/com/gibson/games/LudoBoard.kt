package com.gibson.games;

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Board configuration constants are now expected to be used from BoardConfig.kt

@Composable
fun LudoBoard(
    gameState: GameState,
    viewModel: LudoViewModel, // For click interactions
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(0xFFDDDDDD))) {
        val cellSize = maxWidth / GRID_SIZE // GRID_SIZE from BoardConfig.kt
        val density = LocalDensity.current
        val cellSizePx = with(density) { cellSize.toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBoardBase(cellSizePx)
            drawPlayerYards(cellSizePx, gameState.players)
            drawHomePaths(cellSizePx, gameState.players)
            drawCenterHome(cellSizePx)
            drawTrackCells(cellSizePx, gameState.players, textMeasurer) // Includes start cells and safe zones
        }

        gameState.players.forEach { player ->
            player.pieces.forEach { piece ->
                if (piece.position < 200) { // Don"t draw finished pieces directly on board
                    GamePieceView(
                        piece = piece,
                        playerColor = player.color,
                        cellSize = cellSize,
                        isMovable = gameState.awaitingPieceChoice &&
                                gameState.selectedDiceValue != null &&
                                viewModel.getMovablePiecesForPlayer(
                                    player,
                                    gameState.selectedDiceValue!!,
                                    // Determine if it is a six for yard exit based on selected dice value
                                    (gameState.selectedDiceValue == 6 &&
                                            ((gameState.diceRoll.d1 == 6 && gameState.selectedDiceValue == gameState.diceRoll.d1 && !gameState.diceRoll.d1Used) ||
                                                    (gameState.diceRoll.d2 == 6 && gameState.selectedDiceValue == gameState.diceRoll.d2 && !gameState.diceRoll.d2Used) ||
                                                    (gameState.selectedDiceValue == gameState.diceRoll.sum && (gameState.diceRoll.d1 == 6 || gameState.diceRoll.d2 == 6) && !gameState.diceRoll.d1Used && !gameState.diceRoll.d2Used)))
                                ).any { it.piece.id == piece.id },
                        onClick = {
                            if (player.id == gameState.players[gameState.currentPlayerIndex].id) {
                                viewModel.onPieceClicked(player.id, piece.id)
                            }
                        }
                    )
                }
            }
        }
        gameState.winner?.let {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xAAFFFFFF)), contentAlignment = Alignment.Center){
                Text("${it.name} Wins!", fontSize = 32.sp, color = it.color, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

private fun DrawScope.drawBoardBase(cellSizePx: Float) {
    val boardSizePx = GRID_SIZE * cellSizePx
    drawRect(
        color = Color(0xFFF0F0F0),
        size = Size(boardSizePx, boardSizePx)
    )
}

private fun DrawScope.drawPlayerYards(cellSizePx: Float, players: List<PlayerState>) {
    val yardSizePx = 6 * cellSizePx
    val yardGridPositions = mapOf(
        "red"    to Pair(0f, 0f),
        "green"  to Pair(9 * cellSizePx, 0f),
        "yellow" to Pair(9 * cellSizePx, 9 * cellSizePx), // Yellow bottom right
        "blue"   to Pair(0f, 9 * cellSizePx)  // Blue bottom left
    )

    players.forEach { player ->
        val (offsetX, offsetY) = yardGridPositions[player.id] ?: Pair(0f,0f)
        drawRect(
            color = player.color.copy(alpha = 0.3f),
            topLeft = Offset(offsetX, offsetY),
            size = Size(yardSizePx, yardSizePx)
        )
        drawRect(
            color = player.color,
            topLeft = Offset(offsetX, offsetY),
            size = Size(yardSizePx, yardSizePx),
            style = Stroke(width = 2.dp.toPx())
        )
        val piecePlaceholderRadius = cellSizePx * 0.4f
        // Positions for the 4 piece placeholders in a 2x2 grid within the 6x6 yard
        val placeholderOffsets = listOf(
            Offset(offsetX + 1.5f * cellSizePx, offsetY + 1.5f * cellSizePx),
            Offset(offsetX + 4.5f * cellSizePx, offsetY + 1.5f * cellSizePx),
            Offset(offsetX + 1.5f * cellSizePx, offsetY + 4.5f * cellSizePx),
            Offset(offsetX + 4.5f * cellSizePx, offsetY + 4.5f * cellSizePx)
        )
        placeholderOffsets.forEach {
            drawCircle(color = Color.White, radius = piecePlaceholderRadius, center = it)
            drawCircle(color = player.color.copy(alpha=0.7f), radius = piecePlaceholderRadius, center = it, style = Stroke(1.dp.toPx()))
        }
    }
}

private fun DrawScope.drawHomePaths(cellSizePx: Float, players: List<PlayerState>) {
    players.forEach { player ->
        val pathCoords = playerHomePaths[player.id] ?: emptyList() // from BoardConfig.kt
        pathCoords.forEachIndexed { index, (r, c) ->
            drawRect(
                color = player.color.copy(alpha = if (index == CELLS_IN_HOME_PATH -1) 0.7f else 0.5f), // Last cell slightly different
                topLeft = Offset(c * cellSizePx, r * cellSizePx),
                size = Size(cellSizePx, cellSizePx)
            )
            drawRect(
                color = player.color,
                topLeft = Offset(c * cellSizePx, r * cellSizePx),
                size = Size(cellSizePx, cellSizePx),
                style = Stroke(1.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawCenterHome(cellSizePx: Float) {
    val centerRectSize = 3 * cellSizePx
    val centerOffset = (GRID_SIZE / 2 - 1.5f) * cellSizePx // Centering 3x3 in 15x15

    val pathRed = Path().apply {
        moveTo(centerOffset, centerOffset)
        lineTo(centerOffset + centerRectSize / 2, centerOffset + centerRectSize / 2)
        lineTo(centerOffset, centerOffset + centerRectSize)
        close()
    }
    drawPath(pathRed, PlayerColors.RED.copy(alpha = 0.8f)) // PlayerColors from BoardConfig.kt

    val pathGreen = Path().apply {
        moveTo(centerOffset, centerOffset)
        lineTo(centerOffset + centerRectSize / 2, centerOffset + centerRectSize / 2)
        lineTo(centerOffset + centerRectSize, centerOffset)
        close()
    }
    drawPath(pathGreen, PlayerColors.GREEN.copy(alpha = 0.8f))

    val pathYellow = Path().apply {
        moveTo(centerOffset + centerRectSize, centerOffset)
        lineTo(centerOffset + centerRectSize / 2, centerOffset + centerRectSize / 2)
        lineTo(centerOffset + centerRectSize, centerOffset + centerRectSize)
        close()
    }
    drawPath(pathYellow, PlayerColors.YELLOW.copy(alpha = 0.8f))

    val pathBlue = Path().apply {
        moveTo(centerOffset, centerOffset + centerRectSize)
        lineTo(centerOffset + centerRectSize / 2, centerOffset + centerRectSize / 2)
        lineTo(centerOffset + centerRectSize, centerOffset + centerRectSize)
        close()
    }
    drawPath(pathBlue, PlayerColors.BLUE.copy(alpha = 0.8f))

    drawRect(color = Color.Black, topLeft = Offset(centerOffset, centerOffset), size = Size(centerRectSize, centerRectSize), style = Stroke(2.dp.toPx()))
}

private fun DrawScope.drawTrackCells(cellSizePx: Float, players: List<PlayerState>, textMeasurer: androidx.compose.ui.text.TextMeasurer) {
    mainTrackPath.forEachIndexed { index, (r, c) -> // mainTrackPath from BoardConfig.kt
        var cellColor = Color.White
        var borderColor = Color.Gray

        // Highlight player start cells (actual landing spot from yard)
        players.find { playerStartTrackIndices[it.id] == index }?.let { // playerStartTrackIndices from BoardConfig.kt
            cellColor = it.color.copy(alpha = 0.6f)
            borderColor = it.color
        }

        if (globalSafeTrackIndices.contains(index)) { // globalSafeTrackIndices from BoardConfig.kt
            cellColor = Color.LightGray.copy(alpha = 0.8f) // Mark global safe zones
            // Draw a star or symbol for safe zones
            val starPath = Path().apply {
                val midX = c * cellSizePx + cellSizePx / 2
                val midY = r * cellSizePx + cellSizePx / 2
                val radius = cellSizePx / 4.5f // Adjusted for better look
                val outerRadius = cellSizePx / 2.8f // Adjusted for better look
                for (i in 0..4) {
                    val angle = Math.toRadians((i * 72 - 90).toDouble()).toFloat()
                    val angleInner = Math.toRadians((i * 72 - 90 + 36).toDouble()).toFloat()
                    if (i == 0) moveTo(midX + outerRadius * kotlin.math.cos(angle), midY + outerRadius * kotlin.math.sin(angle))
                    else lineTo(midX + outerRadius * kotlin.math.cos(angle), midY + outerRadius * kotlin.math.sin(angle))
                    lineTo(midX + radius * kotlin.math.cos(angleInner), midY + radius * kotlin.math.sin(angleInner))
                }
                close()
            }
            drawPath(starPath, Color.DarkGray.copy(alpha=0.6f))
        }

        drawRect(
            color = cellColor,
            topLeft = Offset(c * cellSizePx, r * cellSizePx),
            size = Size(cellSizePx, cellSizePx)
        )
        drawRect(
            color = borderColor,
            topLeft = Offset(c * cellSizePx, r * cellSizePx),
            size = Size(cellSizePx, cellSizePx),
            style = Stroke(1.dp.toPx())
        )
        // // Optional: Draw cell index for debugging
        // drawText(
        //     textMeasurer = textMeasurer,
        //     text = index.toString(),
        //     topLeft = Offset(c * cellSizePx + 2.dp.toPx(), r * cellSizePx + 2.dp.toPx()),
        //     style = TextStyle(fontSize = 8.sp, color = Color.Black)
        // )
    }
}

@Composable
fun GamePieceView(
    piece: PieceState,
    playerColor: Color,
    cellSize: Dp,
    isMovable: Boolean,
    onClick: () -> Unit
) {
    val pieceSize = cellSize * 0.8f
    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSize.toPx() }

    val (offsetX, offsetY) = calculatePieceRenderOffset(piece, cellSizePx)

    if (piece.position == 200) return // Finished, not drawn on board

    Box(
        modifier = Modifier
            .size(pieceSize)
            .offset(x = with(density) { offsetX.toDp() }, y = with(density) { offsetY.toDp() })
            .background(piece.color, CircleShape)
            .border(
                width = if (isMovable) 3.dp else 1.5.dp,
                color = if (isMovable) Color.White else playerColor.darker(),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {}
}

fun Color.darker(factor: Float = 0.7f): Color {
    return Color(red * factor, green * factor, blue * factor, alpha)
}

fun calculatePieceRenderOffset(piece: PieceState, cellSizePx: Float): Pair<Float, Float> {
    val pieceActualSizePx = cellSizePx * 0.8f
    val cellPaddingForPiece = (cellSizePx - pieceActualSizePx) / 2

    val yardGridPositions = mapOf(
        "red"    to Pair(0f, 0f),
        "green"  to Pair(9 * cellSizePx, 0f),
        "yellow" to Pair(9 * cellSizePx, 9 * cellSizePx),
        "blue"   to Pair(0f, 9 * cellSizePx)
    )
    // Centering pieces within a 2x2 grid in the 6x6 yard
    val pieceInYardLocalCellOffsets = listOf(
        Pair(1.5f, 1.5f), // Top-left piece in yard (relative to yard's 0,0)
        Pair(4.5f, 1.5f), // Top-right piece in yard
        Pair(1.5f, 4.5f), // Bottom-left piece in yard
        Pair(4.5f, 4.5f)  // Bottom-right piece in yard
    )

    return when {
        piece.position == -1 -> { // In yard
            val (yardBaseX, yardBaseY) = yardGridPositions[piece.playerId] ?: Pair(0f,0f)
            // Get the center of the piece's designated spot in the yard
            val (localCellCenterX, localCellCenterY) = pieceInYardLocalCellOffsets.getOrElse(piece.id) { pieceInYardLocalCellOffsets[0] }
            // Calculate top-left for the piece to center it in its spot
            val pieceTopLeftX = yardBaseX + (localCellCenterX * cellSizePx) - (pieceActualSizePx / 2)
            val pieceTopLeftY = yardBaseY + (localCellCenterY * cellSizePx) - (pieceActualSizePx / 2)
            Pair(pieceTopLeftX, pieceTopLeftY)
        }
        piece.position >= 100 && piece.position < 200 -> { // Home path
            val homePathIndex = piece.position - 100
            val (r, c) = (playerHomePaths[piece.playerId] ?: emptyList()).getOrElse(homePathIndex) { Pair(GRID_SIZE/2,GRID_SIZE/2) } // Fallback to center
            Pair(c * cellSizePx + cellPaddingForPiece, r * cellSizePx + cellPaddingForPiece)
        }
        piece.position >= 0 && piece.position < TOTAL_CELLS_ON_TRACK -> { // Main track
            val (r, c) = mainTrackPath.getOrElse(piece.position) { Pair(GRID_SIZE/2,GRID_SIZE/2) } // Fallback to center
            Pair(c * cellSizePx + cellPaddingForPiece, r * cellSizePx + cellPaddingForPiece)
        }
        else -> Pair((GRID_SIZE/2) * cellSizePx + cellPaddingForPiece, (GRID_SIZE/2) * cellSizePx + cellPaddingForPiece) // Default to center (e.g. error)
    }
}

