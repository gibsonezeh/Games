package com.gibson.games;

import android.app.Activity
import android.app.Application // Required for SoundManager context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel // Changed to AndroidViewModel for Application context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

// Ad Event for UI to observe
sealed class AdTriggerEvent {
    object ShowRewardedInterstitial : AdTriggerEvent()
    object ShowNativeAdvancedGameplay : AdTriggerEvent() // For the 7-min gameplay trigger
    object ShowRewarded : AdTriggerEvent()
}

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    private val _gameState = MutableStateFlow(initializeNewGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val soundManager = SoundManager(application.applicationContext)

    // Gameplay Timer for Ads
    private var gameplayTimerJob: Job? = null
    private var elapsedGameplayTimeSeconds = 0
    private var rewardedInterstitialShown = false
    private var nativeAdvancedGameplayShown = false
    private var rewardedAdShown = false

    private val _adTriggerEvent = MutableSharedFlow<AdTriggerEvent>()
    val adTriggerEvent = _adTriggerEvent.asSharedFlow()

    init {
        soundManager.loadSounds()
    }

    fun startGameplayTimer(activity: Activity) {
        if (gameplayTimerJob?.isActive == true) return // Timer already running
        elapsedGameplayTimeSeconds = 0
        rewardedInterstitialShown = false
        nativeAdvancedGameplayShown = false
        rewardedAdShown = false

        gameplayTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Wait for 1 second
                if (_gameState.value.winner == null) { // Only count time if game is active
                    elapsedGameplayTimeSeconds++
                    Log.d("LudoViewModel", "Gameplay time: $elapsedGameplayTimeSeconds seconds")

                    // Check for time-based ad triggers
                    if (!rewardedInterstitialShown && elapsedGameplayTimeSeconds >= 4 * 60) { // 4 minutes
                        _adTriggerEvent.emit(AdTriggerEvent.ShowRewardedInterstitial)
                        rewardedInterstitialShown = true
                        Log.d("LudoViewModel", "Triggering Rewarded Interstitial Ad (4 mins)")
                    }
                    if (!nativeAdvancedGameplayShown && elapsedGameplayTimeSeconds >= 7 * 60) { // 7 minutes
                         // Native ad in gameplay is tricky, usually shown on screen changes or specific UI spots.
                         // For now, we emit an event. The UI needs a place to show this.
                         // This might be better handled by showing an Interstitial or Rewarded if a dedicated Native slot isn"t in LudoScreen.
                         // User wants Native Ad after 7 mins. We will emit the event.
                        _adTriggerEvent.emit(AdTriggerEvent.ShowNativeAdvancedGameplay) 
                        nativeAdvancedGameplayShown = true
                        Log.d("LudoViewModel", "Triggering Native Advanced Ad (7 mins gameplay) - UI needs to handle this.")
                    }
                    if (!rewardedAdShown && elapsedGameplayTimeSeconds >= 10 * 60) { // 10 minutes
                        _adTriggerEvent.emit(AdTriggerEvent.ShowRewarded)
                        rewardedAdShown = true
                        Log.d("LudoViewModel", "Triggering Rewarded Ad (10 mins)")
                    }
                }
            }
        }
    }

    private fun stopGameplayTimer() {
        gameplayTimerJob?.cancel()
        gameplayTimerJob = null
        Log.d("LudoViewModel", "Gameplay timer stopped.")
    }

    private fun initializeNewGameState(): GameState {
        val players = listOf(
            PlayerState(
                id = "red", name = "Red Player", color = PlayerColors.RED,
                pieces = List(4) { PieceState(id = it, position = -1, color = PlayerColors.RED, playerId = "red") },
                startCellIndex = playerStartTrackIndices["red"]!!,
                homeEntryCellIndex = playerHomeEntryTrackIndices["red"]!!
            ),
            PlayerState(
                id = "green", name = "Green Player", color = PlayerColors.GREEN,
                pieces = List(4) { PieceState(id = it, position = -1, color = PlayerColors.GREEN, playerId = "green") },
                startCellIndex = playerStartTrackIndices["green"]!!,
                homeEntryCellIndex = playerHomeEntryTrackIndices["green"]!!
            ),
            PlayerState(
                id = "yellow", name = "Yellow Player", color = PlayerColors.YELLOW,
                pieces = List(4) { PieceState(id = it, position = -1, color = PlayerColors.YELLOW, playerId = "yellow") },
                startCellIndex = playerStartTrackIndices["yellow"]!!,
                homeEntryCellIndex = playerHomeEntryTrackIndices["yellow"]!!
            ),
            PlayerState(
                id = "blue", name = "Blue Player", color = PlayerColors.BLUE,
                pieces = List(4) { PieceState(id = it, position = -1, color = PlayerColors.BLUE, playerId = "blue") },
                startCellIndex = playerStartTrackIndices["blue"]!!,
                homeEntryCellIndex = playerHomeEntryTrackIndices["blue"]!!
            )
        )
        return GameState(players = players, message = "Red Player, roll the dice!")
    }

    fun onRollDiceClicked() {
        val currentState = _gameState.value
        if (currentState.winner != null) return
        if (currentState.diceRoll.rolled && !(currentState.diceRoll.d1Used && currentState.diceRoll.d2Used)) {
            _gameState.value = currentState.copy(message = "Please use your current roll first.")
            return
        }

        val d1 = Random.nextInt(1, 7)
        val d2 = Random.nextInt(1, 7)
        val newDiceRoll = DiceRoll(d1 = d1, d2 = d2, sum = d1 + d2, rolled = true, d1Used = false, d2Used = false)
        playDiceRollSound()

        val currentPlayer = currentState.players[currentState.currentPlayerIndex]
        val hasPiecesInYard = currentPlayer.pieces.any { it.position == -1 }
        val hasPiecesOnTrack = currentPlayer.pieces.any { it.position >= 0 && it.position < 100 }

        if (!hasPiecesOnTrack && hasPiecesInYard && d1 != 6 && d2 != 6) {
            _gameState.value = currentState.copy(
                diceRoll = newDiceRoll.copy(d1Used = true, d2Used = true),
                message = "Rolled ${d1} & ${d2}. No 6 to exit. Next player."
            )
            viewModelScope.launch {
                switchPlayer(true)
            }
        } else {
            val canMoveWithD1 = getMovablePiecesForPlayer(currentPlayer, d1, d1 == 6).isNotEmpty()
            val canMoveWithD2 = getMovablePiecesForPlayer(currentPlayer, d2, d2 == 6).isNotEmpty()
            val canMoveWithSum = getMovablePiecesForPlayer(currentPlayer, d1 + d2, (d1==6 || d2==6) && (d1+d2==6) ).isNotEmpty()

            if (!canMoveWithD1 && !canMoveWithD2 && !canMoveWithSum && !( (d1==6 || d2==6) && hasPiecesInYard) ) {
                 _gameState.value = currentState.copy(
                    diceRoll = newDiceRoll.copy(d1Used = true, d2Used = true),
                    message = "Rolled ${d1} & ${d2}. No valid moves. Next player."
                )
                viewModelScope.launch {
                    switchPlayer(true)
                }
            } else {
                _gameState.value = currentState.copy(
                    diceRoll = newDiceRoll,
                    message = "Rolled ${d1} & ${d2}. Select a dice value to use.",
                    awaitingPieceChoice = false,
                    selectedDiceValue = null
                )
            }
        }
    }

    fun onDiceValueSelected(valueType: String) {
        val currentState = _gameState.value
        if (currentState.winner != null || !currentState.diceRoll.rolled || currentState.awaitingPieceChoice) {
            return
        }

        val chosenValue: Int?
        val isD1Selected = valueType == "d1"
        val isD2Selected = valueType == "d2"
        val isSumSelected = valueType == "sum"

        chosenValue = when {
            isD1Selected && !currentState.diceRoll.d1Used -> currentState.diceRoll.d1
            isD2Selected && !currentState.diceRoll.d2Used -> currentState.diceRoll.d2
            isSumSelected && !currentState.diceRoll.d1Used && !currentState.diceRoll.d2Used -> currentState.diceRoll.sum
            else -> null
        }

        if (chosenValue == null) {
            _gameState.value = currentState.copy(message = "Invalid dice choice or already used.")
            return
        }

        val currentPlayer = currentState.players[currentState.currentPlayerIndex]
        val isSixForYardExit = chosenValue == 6

        val movablePieces = getMovablePiecesForPlayer(currentPlayer, chosenValue, isSixForYardExit)

        if (movablePieces.isEmpty()) {
            var message = "No valid moves for ${chosenValue}."
            val d1CanMove = if (isD1Selected || currentState.diceRoll.d1Used) false else getMovablePiecesForPlayer(currentPlayer, currentState.diceRoll.d1, currentState.diceRoll.d1 == 6).isNotEmpty()
            val d2CanMove = if (isD2Selected || currentState.diceRoll.d2Used) false else getMovablePiecesForPlayer(currentPlayer, currentState.diceRoll.d2, currentState.diceRoll.d2 == 6).isNotEmpty()
            val sumCanMove = if (isSumSelected || (currentState.diceRoll.d1Used || currentState.diceRoll.d2Used)) false else getMovablePiecesForPlayer(currentPlayer, currentState.diceRoll.sum, (currentState.diceRoll.d1 == 6 || currentState.diceRoll.d2 == 6) && currentState.diceRoll.sum == 6).isNotEmpty()

            if (!d1CanMove && !d2CanMove && !sumCanMove) {
                message += " No other moves possible. Next player."
                 _gameState.value = currentState.copy(
                    diceRoll = currentState.diceRoll.copy(d1Used = true, d2Used = true),
                    message = message
                )
                viewModelScope.launch {
                    switchPlayer(true)
                }
            } else {
                 _gameState.value = currentState.copy(message = "${message} Try another dice value.")
            }
        } else {
            _gameState.value = currentState.copy(
                selectedDiceValue = chosenValue,
                awaitingPieceChoice = true,
                message = "Selected ${chosenValue}. Click a piece to move."
            )
        }
    }

    fun onPieceClicked(playerId: String, pieceId: Int) {
        val currentState = _gameState.value
        if (currentState.winner != null) return
        val currentPlayer = currentState.players[currentState.currentPlayerIndex]

        if (playerId != currentPlayer.id || !currentState.awaitingPieceChoice || currentState.selectedDiceValue == null) {
            return
        }

        val moveValue = currentState.selectedDiceValue!!
        val isSixForYardExit = moveValue == 6

        val movablePieceDetails = getMovablePiecesForPlayer(currentPlayer, moveValue, isSixForYardExit)
            .find { it.piece.id == pieceId }

        if (movablePieceDetails == null) {
            _gameState.value = currentState.copy(message = "This piece cannot make that move with ${moveValue}.")
            return
        }

        var tempPlayers = currentState.players.toMutableList()
        val playerIndexToUpdate = tempPlayers.indexOfFirst { it.id == currentPlayer.id }
        var playerToUpdate = tempPlayers[playerIndexToUpdate]
        
        val newPieces = playerToUpdate.pieces.map {
            if (it.id == pieceId) it.copy(position = movablePieceDetails.newPosition) else it
        }
        playerToUpdate = playerToUpdate.copy(pieces = newPieces)
        tempPlayers[playerIndexToUpdate] = playerToUpdate
        playPieceMoveSound()

        var capturedOpponent = false
        if (movablePieceDetails.newPosition >= 0 && movablePieceDetails.newPosition < TOTAL_CELLS_ON_TRACK) {
            val targetCell = movablePieceDetails.newPosition
            if (!isSafeZone(targetCell)) {
                for (pIdx in tempPlayers.indices) {
                    if (tempPlayers[pIdx].id != currentPlayer.id) {
                        var opponentPlayer = tempPlayers[pIdx]
                        val newOpponentPieces = opponentPlayer.pieces.map { oppPiece ->
                            if (oppPiece.position == targetCell) {
                                capturedOpponent = true
                                oppPiece.copy(position = -1)
                            } else oppPiece
                        }
                        if (opponentPlayer.pieces.any { it.position == targetCell && newOpponentPieces.find { np -> np.id == it.id }?.position == -1 }){
                            tempPlayers[pIdx] = opponentPlayer.copy(pieces = newOpponentPieces)
                        }
                    }
                }
            }
        }
        if (capturedOpponent) playCaptureSound()

        val pieceReachedHome = movablePieceDetails.newPosition == 200
        if (pieceReachedHome) playPieceHomeSound()

        var newDiceRollState = currentState.diceRoll
        if (moveValue == currentState.diceRoll.d1 && !currentState.diceRoll.d1Used) {
            newDiceRollState = newDiceRollState.copy(d1Used = true)
        } else if (moveValue == currentState.diceRoll.d2 && !currentState.diceRoll.d2Used) {
            newDiceRollState = newDiceRollState.copy(d2Used = true)
        } else if (moveValue == currentState.diceRoll.sum && !currentState.diceRoll.d1Used && !currentState.diceRoll.d2Used) {
            newDiceRollState = newDiceRollState.copy(d1Used = true, d2Used = true)
        }
        
        val finalPlayerState = tempPlayers.find{it.id == currentPlayer.id}!!
        val winner = if (finalPlayerState.pieces.all { it.position == 200 }) finalPlayerState else null
        if (winner != null) {
            playWinSound()
            stopGameplayTimer()
            _gameState.value = currentState.copy(
                players = tempPlayers.toList(),
                diceRoll = newDiceRollState,
                winner = winner,
                message = "${winner.name} Wins!",
                awaitingPieceChoice = false,
                selectedDiceValue = null
            )
            return
        }

        val bonusTurnForCaptureOrHome = capturedOpponent || pieceReachedHome
        val bonusTurnForDoubleSix = currentState.diceRoll.d1 == 6 && currentState.diceRoll.d2 == 6

        if (!newDiceRollState.d1Used || !newDiceRollState.d2Used) {
            _gameState.value = currentState.copy(
                players = tempPlayers.toList(),
                diceRoll = newDiceRollState,
                message = "Select remaining dice value.",
                awaitingPieceChoice = false, 
                selectedDiceValue = null
            )
        } else {
            if (bonusTurnForDoubleSix || bonusTurnForCaptureOrHome) {
                _gameState.value = currentState.copy(
                    players = tempPlayers.toList(),
                    diceRoll = DiceRoll(),
                    message = "Bonus turn! ${currentPlayer.name}, roll again.",
                    awaitingPieceChoice = false,
                    selectedDiceValue = null
                )
            } else {
                _gameState.value = currentState.copy(players = tempPlayers.toList(), diceRoll = newDiceRollState)
                switchPlayer(true)
            }
        }
    }

    fun getMovablePiecesForPlayer(player: PlayerState, diceValue: Int, isSixForYardExit: Boolean): List<MovablePieceDetail> {
        val movable = mutableListOf<MovablePieceDetail>()
        player.pieces.forEach { piece ->
            if (piece.position == 200) return@forEach

            if (piece.position == -1) {
                if (isSixForYardExit && diceValue == 6) {
                    val targetPos = player.startCellIndex
                    val startCellOccupiedBySelf = player.pieces.any { it.position == targetPos && it.id != piece.id }
                    if (!startCellOccupiedBySelf) {
                        movable.add(MovablePieceDetail(piece, targetPos))
                    }
                }
            } else if (piece.position >= 100) {
                val currentHomePosIndex = piece.position - 100
                val newHomePosIndex = currentHomePosIndex + diceValue
                if (newHomePosIndex < CELLS_IN_HOME_PATH -1) {
                    movable.add(MovablePieceDetail(piece, 100 + newHomePosIndex))
                } else if (newHomePosIndex == CELLS_IN_HOME_PATH -1) {
                    movable.add(MovablePieceDetail(piece, 200))
                }
            } else {
                val currentTrackPos = piece.position
                var newTrackPos = currentTrackPos
                var stepsTaken = 0
                var enteredHomePath = false

                while(stepsTaken < diceValue) {
                    if (newTrackPos == player.homeEntryCellIndex && (currentTrackPos != player.homeEntryCellIndex || stepsTaken > 0) ) { // Check if it can enter home path
                        val remainingSteps = diceValue - stepsTaken
                        if (remainingSteps <= CELLS_IN_HOME_PATH) {
                            val homePathTargetIndex = remainingSteps - 1 // 0-indexed home path
                            if (homePathTargetIndex == CELLS_IN_HOME_PATH -1) {
                                movable.add(MovablePieceDetail(piece, 200)) // Win
                            } else {
                                movable.add(MovablePieceDetail(piece, 100 + homePathTargetIndex))
                            }
                            enteredHomePath = true
                            break 
                        }
                    }
                    newTrackPos = (newTrackPos + 1) % TOTAL_CELLS_ON_TRACK
                    stepsTaken++
                }
                if (!enteredHomePath) {
                     // Check if newTrackPos is occupied by own piece (not allowed to land on self)
                    val targetCellOccupiedBySelf = player.pieces.any { it.position == newTrackPos && it.id != piece.id }
                    if (!targetCellOccupiedBySelf) {
                        movable.add(MovablePieceDetail(piece, newTrackPos))
                    }
                }
            }
        }
        return movable.distinctBy { "${it.piece.id}-${it.newPosition}" } // Ensure unique moves
    }

    private fun isSafeZone(trackPosition: Int): Boolean {
        return globalSafeTrackIndices.contains(trackPosition)
    }

    private fun switchPlayer(resetDice: Boolean) {
        val currentGameState = _gameState.value
        val nextPlayerIndex = (currentGameState.currentPlayerIndex + 1) % currentGameState.players.size
        _gameState.value = currentGameState.copy(
            currentPlayerIndex = nextPlayerIndex,
            diceRoll = if(resetDice) DiceRoll() else currentGameState.diceRoll, // Reset dice for new player
            message = "${currentGameState.players[nextPlayerIndex].name}, roll the dice!",
            awaitingPieceChoice = false,
            selectedDiceValue = null
        )
    }

    // Sound playing methods
    fun playDiceRollSound() = soundManager.playSound(SoundType.DICE_ROLL)
    private fun playPieceMoveSound() = soundManager.playSound(SoundType.PIECE_MOVE)
    private fun playCaptureSound() = soundManager.playSound(SoundType.PIECE_CAPTURE)
    private fun playPieceHomeSound() = soundManager.playSound(SoundType.PIECE_HOME)
    private fun playWinSound() = soundManager.playSound(SoundType.GAME_WIN)
    fun playBackgroundMusic() = soundManager.playBackgroundMusic()
    fun pauseBackgroundMusic() = soundManager.pauseBackgroundMusic()
    fun stopBackgroundMusic() = soundManager.stopBackgroundMusic()

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        stopGameplayTimer()
        Log.d("LudoViewModel", "ViewModel cleared, resources released.")
    }
}

data class MovablePieceDetail(val piece: PieceState, val newPosition: Int)

