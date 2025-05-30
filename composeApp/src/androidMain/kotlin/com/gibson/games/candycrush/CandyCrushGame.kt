package com.gibson.games.candycrush

import android.os.Bundle
import androidx.activity.ComponentActivity 
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.* 
import androidx.compose.material3.* 
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment 
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp 
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gibson.games.GamesApp
import com.gibson.games.candycrush.CandyCrushScreen

class MainActivity : ComponentActivity() { 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GamesApp()
        }
    } 
}

@Composable 
fun GamesApp() {
    var selectedGame by remember {
        mutableStateOf<String?>(null) 
    }

MaterialTheme {
    Surface(modifier = Modifier.fillMaxSize()) {
        when (selectedGame) {
            "candy_crush" -> CandyCrushScreen(viewModel = viewModel(), onBack = { selectedGame = null })
            else -> GameMenu(onGameSelected = { selectedGame = it })
        }
    }
}

}

@Composable
fun GameMenu(onGameSelected: (String) -> Unit) {
    Column( modifier = Modifier .fillMaxSize() .padding(24.dp), 
           verticalArrangement = Arrangement.spacedBy(16.dp),
           horizontalAlignment = Alignment.CenterHorizontally ) { 
        Text("🎮 Multi Game App", fontSize = 28.sp)

Button(onClick = { onGameSelected("candy_crush") }) {
        Text("🍬 Play Candy Crush")
    }

    Button(onClick = { /* onGameSelected("tetris") */ }, enabled = false) {
        Text("🧱 Tetris (Coming Soon)")
    }

    Button(onClick = { /* onGameSelected("sudoku") */ }, enabled = false) {
        Text("🔢 Sudoku (Coming Soon)")
    }
}

}

