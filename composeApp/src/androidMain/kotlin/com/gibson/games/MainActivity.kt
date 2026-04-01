package com.gibson.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gibson.games.ui.MultiGameApp
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 Initialize AdMob
        MobileAds.initialize(this)

        setContent {
            MultiGameApp()
        }
    }
}
