package com.gibson.games

import android.app.Activity // Required for AdMob context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LudoGameScreen(viewModel: LudoViewModel, activity: Activity) { // Added activity parameter
    val gameState by viewModel.gameState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Attempt to play background music when the screen is first composed.
    LaunchedEffect(Unit) {
        viewModel.playBackgroundMusic()
        viewModel.startGameplayTimer(activity) // Start gameplay timer for ads
    }

    // Show Interstitial Ad when game ends
    LaunchedEffect(gameState.winner) {
        if (gameState.winner != null) {
            // Delay slightly to allow win message to be seen
            coroutineScope.launch {
                delay(1500) // 1.5 seconds delay
                InterstitialAdManager.showAd(activity)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ludo King - ${gameState.players[gameState.currentPlayerIndex].name}'s Turn") }) 
        },
        bottomBar = {
            // AdMob Banner Ad at the bottom of Ludo Game Screen
            AdMobBanner(adUnitId = "ca-app-pub-8105096464664625/6118918264", activity = activity)
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize().padding(it), color = MaterialTheme.colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerTurnIndicator(player = gameState.players[gameState.currentPlayerIndex])
                MessageArea(message = gameState.message)

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(Color.DarkGray), // Background for the board container
                    contentAlignment = Alignment.Center
                ) {
                    LudoBoard(gameState = gameState, viewModel = viewModel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                DiceArea(
                    diceRoll = gameState.diceRoll,
                    onRollDice = { viewModel.onRollDiceClicked() },
                    onDiceValueSelected = { valueType -> viewModel.onDiceValueSelected(valueType) },
                    enabled = gameState.winner == null,
                    currentPlayerColor = gameState.players[gameState.currentPlayerIndex].color
                )
            }
        }
    }
}

@Composable
fun PlayerTurnIndicator(player: PlayerState) {
    Text(
        text = "Turn: ${player.name}",
        color = player.color,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun MessageArea(message: String) {
    Text(
        text = message,
        fontSize = 16.sp,
        modifier = Modifier.padding(vertical = 8.dp).height(40.dp) // Give some fixed height for messages
    )
}

@Composable
fun DiceArea(
    diceRoll: DiceRoll,
    onRollDice: () -> Unit,
    onDiceValueSelected: (String) -> Unit,
    enabled: Boolean,
    currentPlayerColor: Color // Pass current player color for dice highlight
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            DiceView(value = diceRoll.d1, isUsed = diceRoll.d1Used, isRolled = diceRoll.rolled, enabled = enabled, onClick = { onDiceValueSelected("d1") }, availableColor = currentPlayerColor)
            DiceView(value = diceRoll.d2, isUsed = diceRoll.d2Used, isRolled = diceRoll.rolled, enabled = enabled, onClick = { onDiceValueSelected("d2") }, availableColor = currentPlayerColor)
            DiceView(value = diceRoll.sum, isUsed = diceRoll.d1Used && diceRoll.d2Used, isRolled = diceRoll.rolled, enabled = enabled, onClick = { onDiceValueSelected("sum") }, availableColor = currentPlayerColor, isSum = true, d1Used = diceRoll.d1Used, d2Used = diceRoll.d2Used)
        }
        Button(
            onClick = onRollDice,
            enabled = enabled && (!diceRoll.rolled || (diceRoll.d1Used && diceRoll.d2Used)) // Enable roll if not rolled, or if both dice used
        ) {
            Text("Roll Dice")
        }
    }
}

@Composable
fun DiceView(
    value: Int,
    isUsed: Boolean,
    isRolled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    availableColor: Color,
    isSum: Boolean = false,
    d1Used: Boolean? = null, // Only for sum, to know if individual dice are used
    d2Used: Boolean? = null  // Only for sum
) {
    val canBeClicked = enabled && isRolled &&
            (if (isSum) !(d1Used!! && d2Used!!) && !isUsed else !isUsed)

    val backgroundColor = when {
        !isRolled -> Color.White // Not rolled yet
        isUsed -> Color.LightGray // Rolled and used
        else -> availableColor.copy(alpha = 0.7f) // Rolled and available, use player color with alpha
    }
    val textColor = if (isUsed || !isRolled) Color.DarkGray else Color.Black

    Box(
        modifier = Modifier
            .size(60.dp) // Slightly larger dice
            .background(backgroundColor)
            .border(1.5.dp, Color.DarkGray)
            .clickable(enabled = canBeClicked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isRolled && value > 0) {
            Text(text = value.toString(), fontSize = 28.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreviewLudoScreen() {
    val dummyPlayers = listOf(
        PlayerState("red", "Red Player", PlayerColors.RED, List(4) { PieceState(it, -1, PlayerColors.RED, "red") }, playerStartTrackIndices["red"]!!, playerHomeEntryTrackIndices["red"]!!),
        PlayerState("green", "Green Player", PlayerColors.GREEN, List(4) { PieceState(it, -1, PlayerColors.GREEN, "green") }, playerStartTrackIndices["green"]!!, playerHomeEntryTrackIndices["green"]!!),
        PlayerState("yellow", "Yellow Player", PlayerColors.YELLOW, List(4) { PieceState(it, -1, PlayerColors.YELLOW, "yellow") }, playerStartTrackIndices["yellow"]!!, playerHomeEntryTrackIndices["yellow"]!!),
        PlayerState("blue", "Blue Player", PlayerColors.BLUE, List(4) { PieceState(it, -1, PlayerColors.BLUE, "blue") }, playerStartTrackIndices["blue"]!!, playerHomeEntryTrackIndices["blue"]!!)
    )
    val dummyGameState = GameState(
        players = dummyPlayers,
        currentPlayerIndex = 0,
        diceRoll = DiceRoll(d1 = 3, d2 = 4, sum = 7, rolled = true, d1Used = false, d2Used = false),
        message = "Red Player, select a dice value.",
        awaitingPieceChoice = false,
        selectedDiceValue = null,
        winner = null
    )
    val context = LocalContext.current

    MaterialTheme {
        LudoGameScreen(viewModel = LudoViewModel(context.applicationContext), activity = context as Activity)
    }
}

