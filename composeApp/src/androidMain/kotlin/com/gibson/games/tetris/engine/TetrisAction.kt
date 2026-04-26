sealed interface TetrisAction {
    data class Move(val direction: Direction) : TetrisAction

    data object Rotate : TetrisAction
    data object Drop : TetrisAction

    data object Reset : TetrisAction
    data object Pause : TetrisAction
    data object Resume : TetrisAction
    data object GameTick : TetrisAction
    data object Mute : TetrisAction

    // 👇 NEW
    data object StartGame : TetrisAction
    data class SetLevel(val delta: Int) : TetrisAction
    data class SetStartLines(val delta: Int) : TetrisAction
}
