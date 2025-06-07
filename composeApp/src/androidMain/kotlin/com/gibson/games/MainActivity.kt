package com.gibson.games

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gibson.games.platform.input.AndroidSwipeDetector
import com.gibson.games.ui.MenuScreen
import com.gibson.games.core.SwipeDirection
import com.gibson.games.core.SwipeInputHandler // Optional interface or object to relay swipes

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val swipeDetector = AndroidSwipeDetector { direction ->
            // Dispatch to global swipe handler (to be implemented)
            SwipeInputHandler.handleSwipe(direction)
        }

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {

                // Invisible AndroidView to capture swipe gestures
                AndroidView(
                    factory = { context ->
                        View(context).apply {
                            setOnTouchListener(swipeDetector)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Game UI on top
                MenuScreen()
            }
        }
    }
}
