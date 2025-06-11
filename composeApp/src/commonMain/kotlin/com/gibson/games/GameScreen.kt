
/**
 * A wrapper screen for the selected game. It handles the top-level back navigation.
 */
@Composable
fun GameScreen(game: Game, onExit: () -> Unit) {
    // This BackHandler is for exiting the game entirely and returning to the GameMenu.
    // It will be triggered if no other BackHandler consumes the event.
    BackHandler {
        onExit()
    }

    when (game) {
        Game.Ludo -> LudoApp(onExit = onExit)
        // Add other games here
    }
}
