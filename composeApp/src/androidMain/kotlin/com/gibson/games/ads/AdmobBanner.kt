package com.gibson.games.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner() {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)

                // 🔥 TEST AD UNIT (SAFE)
                adUnitId = "ca-app-pub-8105096464664625/6118918264"

                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
