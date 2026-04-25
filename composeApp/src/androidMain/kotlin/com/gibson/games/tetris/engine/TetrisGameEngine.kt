package com.gibson.games.tetris.engine

class TetrisGameEngine {

    fun createInitialState(): TetrisState {
        val reserve = generateTetrominoReserve()

        return TetrisState(
            currentPiece = reserve.first(),
            nextPieces = reserve.drop(1),
            gameStatus = TetrisGameStatus.Running
        )
    }

    fun getGhostPiece(state: TetrisState): Tetromino {
    if (!state.isRunning) return Tetromino.Empty

    var step = 0

    while (
        state.currentPiece
            .moveBy(0, step + 1)
            .isValidInMatrix(
                bricks = state.bricks,
                matrixWidth = state.matrixWidth,
                matrixHeight = state.matrixHeight
            )
    ) {
        step++
    }

    return state.currentPiece.moveBy(0, step)
}
    fun dispatch(
        state: TetrisState,
        action: TetrisAction
    ): TetrisState {
        return when (action) {

            TetrisAction.Reset -> createInitialState().copy(
                isMute = state.isMute
            )

            TetrisAction.Pause -> {
                if (state.isRunning) {
                    state.copy(gameStatus = TetrisGameStatus.Paused)
                } else {
                    state
                }
            }

            TetrisAction.Resume -> {
                if (state.isPaused) {
                    state.copy(gameStatus = TetrisGameStatus.Running)
                } else {
                    state
                }
            }

            is TetrisAction.Move -> move(state, action.direction)

            TetrisAction.Rotate -> rotate(state)

            TetrisAction.Drop -> drop(state)

            TetrisAction.GameTick -> tick(state)

            TetrisAction.Mute -> state.copy(
                isMute = !state.isMute
            )
        }
    }

    private fun move(
        state: TetrisState,
        direction: Direction
    ): TetrisState {
        if (!state.isRunning) return state

        val movedPiece = state.currentPiece.moveBy(
            dx = direction.dx,
            dy = direction.dy
        )

        return if (movedPiece.isValidInMatrix(state.bricks, state.matrixWidth, state.matrixHeight)) {
            state.copy(currentPiece = movedPiece)
        } else {
            state
        }
    }

    private fun rotate(state: TetrisState): TetrisState {
        if (!state.isRunning) return state

        val rotatedPiece = state.currentPiece
            .rotate()
            .adjustOffset(
                matrixWidth = state.matrixWidth,
                matrixHeight = state.matrixHeight
            )

        return if (rotatedPiece.isValidInMatrix(state.bricks, state.matrixWidth, state.matrixHeight)) {
            state.copy(currentPiece = rotatedPiece)
        } else {
            state
        }
    }

    private fun drop(state: TetrisState): TetrisState {
        if (!state.isRunning) return state

        var step = 0

        while (
            state.currentPiece
                .moveBy(0, step + 1)
                .isValidInMatrix(state.bricks, state.matrixWidth, state.matrixHeight)
        ) {
            step++
        }

        return state.copy(
            currentPiece = state.currentPiece.moveBy(0, step)
        )
    }

    private fun tick(state: TetrisState): TetrisState {
        if (!state.isRunning) return state

        val fallingPiece = state.currentPiece.moveBy(
            dx = Direction.Down.dx,
            dy = Direction.Down.dy
        )

        if (fallingPiece.isValidInMatrix(state.bricks, state.matrixWidth, state.matrixHeight)) {
            return state.copy(currentPiece = fallingPiece)
        }

        if (!state.currentPiece.isValidInMatrix(state.bricks, state.matrixWidth, state.matrixHeight)) {
            return state.copy(gameStatus = TetrisGameStatus.GameOver)
        }

        val result = updateBricks(
            currentBricks = state.bricks,
            piece = state.currentPiece,
            matrixWidth = state.matrixWidth
        )

        val newReserve = state.nextPieces.drop(1).takeIf { it.isNotEmpty() }
            ?: generateTetrominoReserve(state.matrixWidth)

        return state.copy(
            bricks = result.clearedBricks,
            currentPiece = state.nextPiece,
            nextPieces = newReserve,
            score = state.score + calculateScore(result.clearedLines) + SCORE_EVERY_PIECE,
            lines = state.lines + result.clearedLines,
            gameStatus = TetrisGameStatus.Running
        )
    }

    private fun updateBricks(
        currentBricks: List<Brick>,
        piece: Tetromino,
        matrixWidth: Int
    ): LineClearResult {
        val allBricks = currentBricks + Brick.of(piece)

        val rows = allBricks.groupBy { it.y }

        val linesToClear = rows
            .filter { (_, bricks) -> bricks.size == matrixWidth }
            .keys
            .sorted()

        if (linesToClear.isEmpty()) {
            return LineClearResult(
                clearedBricks = allBricks,
                clearedLines = 0
            )
        }

        var cleared = allBricks

        linesToClear.forEach { line ->
            cleared = cleared
                .filter { it.y != line }
                .map { brick ->
                    if (brick.y < line) {
                        brick.offsetBy(0, 1)
                    } else {
                        brick
                    }
                }
        }

        return LineClearResult(
            clearedBricks = cleared,
            clearedLines = linesToClear.size
        )
    }
}

data class LineClearResult(
    val clearedBricks: List<Brick>,
    val clearedLines: Int
)
