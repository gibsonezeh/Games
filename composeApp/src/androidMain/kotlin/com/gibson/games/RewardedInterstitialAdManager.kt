package com.gibson.games

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object RewardedInterstitialAdManager {

    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isLoading: Boolean = false
    private const val AD_UNIT_ID = "ca-app-pub-8105096464664625/9695201197"

    fun loadAd(context: Context) {
        if (isLoading || mRewardedInterstitialAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("RewardedInterstitialAdManager", "Rewarded Interstitial ad failed to load: ${adError.message}")
                    mRewardedInterstitialAd = null
                    isLoading = false
                }

                override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    Log.d("RewardedInterstitialAdManager", "Rewarded Interstitial ad loaded.")
                    mRewardedInterstitialAd = rewardedInterstitialAd
                    isLoading = false
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: () -> Unit = {},
        onUserEarnedReward: (com.google.android.gms.ads.rewarded.RewardItem) -> Unit = {}
    ) {
        if (mRewardedInterstitialAd != null) {
            mRewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("RewardedInterstitialAdManager", "Ad was dismissed.")
                    mRewardedInterstitialAd = null // Ad can only be shown once
                    loadAd(activity.applicationContext) // Preload the next ad
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("RewardedInterstitialAdManager", "Ad failed to show: ${adError.message}")
                    mRewardedInterstitialAd = null
                    onAdFailedToShow()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("RewardedInterstitialAdManager", "Ad showed fullscreen content.")
                }
            }
            mRewardedInterstitialAd?.show(activity, OnUserEarnedRewardListener {
                Log.d("RewardedInterstitialAdManager", "User earned reward: ${it.amount} ${it.type}")
                onUserEarnedReward(it)
            })
        } else {
            Log.d("RewardedInterstitialAdManager", "The rewarded interstitial ad wasn"t ready yet.")
            loadAd(activity.applicationContext) // Try to load an ad if not available
            onAdFailedToShow()
        }
    }
}

