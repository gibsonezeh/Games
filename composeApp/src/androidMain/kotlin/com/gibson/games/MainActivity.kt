// androidMain/com/gibson/games/MainActivity.kt

package com.gibson.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gibson.games.ui.MultiGameApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiGameApp()
        }
    }
}
