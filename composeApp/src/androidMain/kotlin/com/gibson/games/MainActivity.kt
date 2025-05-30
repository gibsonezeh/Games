package com.gibson.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment 
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp 
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gibson.games.candycrush.CandyCrushScreen
import com.gibson.games.candycrush.CandyCrushViewModel

class MainActivity : ComponentActivity() {
      val candyCrushViewModel: CandyCrushViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        GamesApp(viewModel = candyCrushViewModel)
        }
    }
}
