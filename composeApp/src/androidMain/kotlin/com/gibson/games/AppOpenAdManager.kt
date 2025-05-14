package com.gibson.games

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * Manages loading and showing App Open Ads.
 * This class should be instantiated once, typically in the Application class.
 */
class AppOpenAdManager(private val context: Context) {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    // Your App Open Ad Unit ID
    private val AD_UNIT_ID = "ca-app-pub-8105096464664625/3196099815"

    private var loadTime: Long = 0

    init {
        // Optionally load an ad when the manager is initialized
        // loadAd()
    }

    /** Loads an AppOpenAd. */
    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, // Or APP_OPEN_AD_ORIENTATION_LANDSCAPE
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d("AppOpenAdManager", "App Open Ad loaded.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    appOpenAd = null // Ensure ad is null on failure
                    Log.e("AppOpenAdManager", "App Open Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /** Checks if an ad is available to show. */
    private fun isAdAvailable(): Boolean {
        // An ad is available if it was loaded more than an hour ago.
        // Note: App Open ads expire after four hours.
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Shows the ad if available. */
    fun showAdIfAvailable(activity: Activity, onShowFullScreenContent: () -> Unit = {}) {
        if (isShowingAd) {
            Log.d("AppOpenAdManager", "The app open ad is already showing.")
            return
        }

        if (!isAdAvailable()) {
            Log.d("AppOpenAdManager", "The app open ad is not ready yet.")
            loadAd() // Load the next ad
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Called when full screen content is dismissed.
                // Set the reference to null so isAdAvailable() returns false.
                appOpenAd = null
                isShowingAd = false
                Log.d("AppOpenAdManager", "App Open Ad dismissed.")
                loadAd() // Load the next ad
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                // Set the reference to null so isAdAvailable() returns false.
                appOpenAd = null
                isShowingAd = false
                Log.e("AppOpenAdManager", "App Open Ad failed to show: ${adError.message}")
                loadAd() // Load the next ad
            }

            override fun onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                isShowingAd = true
                Log.d("AppOpenAdManager", "App Open Ad showed.")
                onShowFullScreenContent()
            }
        }
        appOpenAd?.show(activity)
    }
}

