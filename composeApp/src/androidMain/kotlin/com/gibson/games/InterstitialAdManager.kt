package com.gibson.games

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false
    private const val AD_UNIT_ID = "ca-app-pub-8105096464664625/5115702056"

    fun loadAd(context: Context) {
        if (isLoading || mInterstitialAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialAdManager", "Interstitial ad failed to load: ${adError.message}")
                    mInterstitialAd = null
                    isLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("InterstitialAdManager", "Interstitial ad loaded.")
                    mInterstitialAd = interstitialAd
                    isLoading = false
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit = {}, onAdFailedToShow: () -> Unit = {}) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialAdManager", "Ad was dismissed.")
                    mInterstitialAd = null // Ad can only be shown once
                    loadAd(activity.applicationContext) // Preload the next ad
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("InterstitialAdManager", "Ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    onAdFailedToShow()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("InterstitialAdManager", "Ad showed fullscreen content.")
                    // Called when ad isDismissedFullScreenContent.
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAdManager", "The interstitial ad wasn"t ready yet.")
            loadAd(activity.applicationContext) // Try to load an ad if not available
            onAdFailedToShow() // Indicate ad wasn"t shown
        }
    }
}

