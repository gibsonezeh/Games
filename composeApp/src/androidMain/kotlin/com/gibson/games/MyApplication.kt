package com.gibson.games

import android.app.Application
import com.google.android.gms.ads.MobileAds

/**
 * Application class to initialize Mobile Ads SDK and AppOpenAdManager.
 */
class MyApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}

        // Initialize the AppOpenAdManager and load the first ad.
        appOpenAdManager = AppOpenAdManager(this)
        appOpenAdManager.loadAd() // Pre-load an ad
    }
}

