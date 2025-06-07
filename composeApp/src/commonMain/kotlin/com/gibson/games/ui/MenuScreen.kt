package com.gibson.games.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gibson.games.ui.GameSelector

@Composable
fun MenuScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎮 Game Hub",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            // Game Selection UI
            GameSelector()

            // Placeholder for other buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { /* TODO: Add settings logic */ }) {
                    Text("Settings")
                }
                Button(onClick = { /* TODO: Add exit logic */ }) {
                    Text("Exit")
                }
            }
        }
    }
}
