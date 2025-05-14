package com.gibson.games

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier,
    activity: Activity // Pass activity for context
) {
    val context = LocalContext.current
    val adView = remember { AdView(context) }

    DisposableEffect(adUnitId, activity) {
        adView.adUnitId = adUnitId
        adView.setAdSize(AdSize.BANNER) // Or AdSize.ADAPTIVE_BANNER if you calculate width

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("AdMobBanner", "Banner Ad loaded: $adUnitId")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("AdMobBanner", "Banner Ad failed to load: $adUnitId, Error: ${loadAdError.message}")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(AdSize.BANNER.getHeightInDp(context).dp), // Adjust height based on AdSize
        factory = { 
            // Ensure AdView is added to a ViewGroup. FrameLayout is a simple option.
            FrameLayout(it).apply {
                addView(adView)
            }
        }
    )
}

