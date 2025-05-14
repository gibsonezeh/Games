package com.gibson.games

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {

    private var mRewardedAd: RewardedAd? = null
    private var isLoading: Boolean = false
    private const val AD_UNIT_ID = "ca-app-pub-8105096464664625/6717684064"

    fun loadAd(context: Context) {
        if (isLoading || mRewardedAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("RewardedAdManager", "Rewarded ad failed to load: ${adError.message}")
                    mRewardedAd = null
                    isLoading = false
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d("RewardedAdManager", "Rewarded ad loaded.")
                    mRewardedAd = rewardedAd
                    isLoading = false
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: () -> Unit = {},
        onUserEarnedReward: (RewardItem) -> Unit = {}
    ) {
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("RewardedAdManager", "Ad was dismissed.")
                    mRewardedAd = null // Ad can only be shown once
                    loadAd(activity.applicationContext) // Preload the next ad
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("RewardedAdManager", "Ad failed to show: ${adError.message}")
                    mRewardedAd = null
                    onAdFailedToShow()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("RewardedAdManager", "Ad showed fullscreen content.")
                    // Called when ad isDismissedFullScreenContent.
                }
            }
            mRewardedAd?.show(activity, OnUserEarnedRewardListener {
                Log.d("RewardedAdManager", "User earned reward: ${it.amount} ${it.type}")
                onUserEarnedReward(it)
            })
        } else {
            Log.d("RewardedAdManager", "The rewarded ad wasn"t ready yet.")
            loadAd(activity.applicationContext) // Try to load an ad if not available
            onAdFailedToShow()
        }
    }
}

