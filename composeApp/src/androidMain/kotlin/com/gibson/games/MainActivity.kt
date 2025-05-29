package com.gibson.games

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gibson.games.dreamtreats.DreamTreatsActivity // Import your game activity
import com.gibson.games.ui.theme.GamesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameSelectionScreen { gameName ->
                        when (gameName) {
                            "DreamTreats" -> {
                                startActivity(Intent(this, DreamTreatsActivity::class.java))
                            }
                            // Add more games here
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameSelectionScreen(onGameSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Choose a Game", style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = { onGameSelected("DreamTreats") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("DreamTreats (Candy Crush)")
        }
        // Add more game buttons here as you create them
        // Button(onClick = { onGameSelected("AnotherGame") }) {
        //     Text("Another Game")
        // }
    }
}

@Preview(showBackground = true)
@Composable
fun GameSelectionScreenPreview() {
    GamesTheme {
        GameSelectionScreen {}
    }
}
