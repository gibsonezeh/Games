package com.example.ludo

import kotlin.random.Random

enum class PlayerColor { GREEN, RED, YELLOW, BLUE }

enum class GamePhase { ROLLING, MOVING, GAME_OVER }

data class Token(val id: Int, 
                 val color: PlayerColor,
                 val position: Int = -1)

data class Player( val color: PlayerColor,
                  val tokens: List<Token> = List(4) { Token(it, color) },
                  val diceRolls: MutableList<Int> = mutableListOf() )

data class DiceRoll(val die1: Int,
                    val die2: Int) { 
    val total: Int get() = die1 + die2 
    val isDoubleSix: Boolean get() = die1 == 6 && die2 == 6
    val isTripleSix: Boolean get() = false // track outside if needed }

data class GameRules( val requiresSixToExitBase: Boolean = true,
                     val getsExtraTurnOnSix: Boolean = true,
                     val getsExtraTurnOnCapture: Boolean = true, 
                     val threeSixesForfeitTurn: Boolean = true,
                     val captureReturnsToBase: Boolean = true,
                     val safeZones: List<Int> = listOf(1, 9, 14, 22, 27, 35, 40, 48) )

data class BoardState( val players: List<Player>, 
                      val currentPlayer: PlayerColor,
                      val gamePhase: GamePhase = GamePhase.ROLLING, 
                      val diceRoll: DiceRoll? = null, 
                      val winner: PlayerColor? = null )

fun initializeGame(): BoardState { val players = PlayerColor.values().map { Player(it) }
    return BoardState(players = players, currentPlayer = PlayerColor.GREEN) }

fun rollDice(): DiceRoll = DiceRoll(Random.nextInt(1, 7), Random.nextInt(1, 7))

fun moveToken(token: Token, steps: Int, boardState: BoardState, rules: GameRules): Token { 
    if (token.position == -1 && steps == 6 && rules.requiresSixToExitBase) {
        return token.copy(position = getStartPosition(token.color)) } 
    if (token.position in 0..51) { val homeEntry = getHomeEntryPosition(token.color)
        val distToEntry = if (token.position <= homeEntry) homeEntry - token.position 
        else 52 - token.position + homeEntry 
        return if (steps > distToEntry) { token.copy(position = 100 + (steps - distToEntry - 1)) } 
        else { token.copy(position = (token.position + steps) % 52) } }
    if (token.position in 100..105 && token.position + steps <= 105) { 
        return token.copy(position = token.position + steps) }
    return token }

fun getStartPosition(color: PlayerColor): Int = when (color) { PlayerColor.GREEN -> 1 PlayerColor.RED -> 14 PlayerColor.YELLOW -> 27 PlayerColor.BLUE -> 40 }

fun getHomeEntryPosition(color: PlayerColor): Int = when (color) { PlayerColor.GREEN -> 51 PlayerColor.RED -> 12 PlayerColor.YELLOW -> 25 PlayerColor.BLUE -> 38 }

fun isSafeZone(position: Int, rules: GameRules): Boolean = position in rules.safeZones || position in 100..105 || position == 200

fun checkCapture(movedToken: Token, boardState: BoardState, rules: GameRules): Pair<BoardState, Boolean> {
    if (movedToken.position in 0..51 && rules.captureReturnsToBase) { 
    val opponents = boardState.players.filter { it.color != movedToken.color } opponents.forEach { opponent -> opponent.tokens.find { it.position == movedToken.position && !isSafeZone(it.position, rules) }?.let { captured ->
    val updatedPlayers = boardState.players.map { 
        if (it.color == opponent.color) { it.copy(tokens = it.tokens.map { t -> 
        if (t.id == captured.id) t.copy(position = -1) 
        else t }) } 
        else it }
        return boardState.copy(players = updatedPlayers) to true } } }
    return boardState to false }

fun checkWin(player: Player): Boolean = player.tokens.all { it.position == 200 }

fun applyMove(token: Token, diceRoll: DiceRoll, state: BoardState, rules: GameRules): BoardState { 
    val totalSteps = diceRoll.total
    val movedToken = moveToken(token, totalSteps, state, rules) 
    val updatedPlayers = state.players.map { 
        if (it.color == token.color) { it.copy(tokens = it.tokens.map { t -> 
            if (t.id == token.id) movedToken
            else t }) }
        else it }
    val updatedState = state.copy(players = updatedPlayers)
    val (finalState, didCapture) = checkCapture(movedToken, updatedState, rules) 
    val currentPlayer = finalState.players.first { it.color == token.color }
    return if (checkWin(currentPlayer)) { finalState.copy(gamePhase = GamePhase.GAME_OVER, winner = token.color) } 
    else { 
        val keepTurn = (diceRoll.die1 == 6 || diceRoll.die2 == 6) && rules.getsExtraTurnOnSix || (didCapture && rules.getsExtraTurnOnCapture) finalState.copy( currentPlayer = 
                if (keepTurn) token.color 
                                 else getNextPlayer(token.color), gamePhase = GamePhase.ROLLING ) } }

fun getNextPlayer(current: PlayerColor): PlayerColor = when (current) { PlayerColor.GREEN -> PlayerColor.RED PlayerColor.RED -> PlayerColor.YELLOW PlayerColor.YELLOW -> PlayerColor.BLUE PlayerColor.BLUE -> PlayerColor.GREEN }

