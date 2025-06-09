package com.gibson.games.ludo

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier


@Composable
fun LudoGameScreen(onExit: () -> Unit) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = {Text("Ludo game ")},
                actions = {
                    Button(onClick = onExit) {
                        Text("Exit")
                    }
                }
            )
        }
    ){
        padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ){
            Text("Ludo Game", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
