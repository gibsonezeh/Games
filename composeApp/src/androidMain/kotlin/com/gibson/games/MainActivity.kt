// MainActivity.kt
package com.gibson.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.gibson.games.candycrush.CandyCrushScreen
import com.gibson.games.candycrush.CandyCrushViewModel

class MainActivity : ComponentActivity() {
    private val candyCrushViewModel: CandyCrushViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CandyCrushScreen(viewModel = candyCrushViewModel, onBack = {})
        }
    }
}
