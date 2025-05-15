package com.gibson.games

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var nativeAd: NativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)

        loadInterstitialAd()
        loadRewardedAd()
        loadRewardedInterstitialAd()
        loadAppOpenAd()
        loadNativeAd()

        setContent {
            GameApp(this)
        }
    }

    override fun onResume() {
        super.onResume()
        appOpenAd?.show(this)
    }


    fun loadAppOpenAd() {
    val adRequest = AdRequest.Builder().build()
    AppOpenAd.load(
        this,
        "ca-app-pub-8105096464664625/3196099815", // your App Open Ad ID
        adRequest,
        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                appOpenAd = null
            }
        }
    )
    }

    fun showRewardedAd() {
        rewardedAd?.show(this) { _: RewardItem -> }
        loadRewardedAd()
    }

    fun showRewardedInterstitialAd() {
        rewardedInterstitialAd?.show(this) { _: RewardItem -> }
        loadRewardedInterstitialAd()
    }

    fun showInterstitialAd() {
        interstitialAd?.show(this)
        loadInterstitialAd()
    }

    fun loadNativeAd() {
        val builder = AdLoader.Builder(this, "ca-app-pub-8105096464664625/9250388185")
        builder.forNativeAd {
            nativeAd = it
        }
        builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                nativeAd = null
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build().loadAd(AdRequest.Builder().build())
    }

    fun loadInterstitialAd() {
        InterstitialAd.load(this,
            "ca-app-pub-8105096464664625/5115702056",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun loadRewardedAd() {
        RewardedAd.load(this,
            "ca-app-pub-8105096464664625/6717684064",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    fun loadRewardedInterstitialAd() {
        RewardedInterstitialAd.load(this,
            "ca-app-pub-8105096464664625/9695201197",
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                }
            })
    }

    fun getNativeAd(): NativeAd? = nativeAd
}

// -- COMPOSABLES --

@Composable
fun GameApp(activity: MainActivity) {
    var selectedGame by remember { mutableStateOf<String?>(null) }

    LaunchedEffect("rewarded_timer") {
        while (true) {
            delay(2 * 60 * 1000)
            activity.showRewardedAd()
        }
    }

    LaunchedEffect("rewarded_interstitial_timer") {
        while (true) {
            delay(5 * 60 * 1000)
            activity.showRewardedInterstitialAd()
        }
    }

    LaunchedEffect("interstitial_timer") {
        while (true) {
            delay(7 * 60 * 1000)
            activity.showInterstitialAd()
        }
    }

    LaunchedEffect("native_ad_timer") {
        while (true) {
            delay(10 * 60 * 1000)
            activity.loadNativeAd()
        }
    }

    if (selectedGame == null) {
        GameMenu { selectedGame = it }
    } else {
        GameScreen("file:///android_asset/games/$selectedGame/$selectedGame.html", { selectedGame = null }, activity)
    }
}

@Composable
fun GameMenu(onSelectGame: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Choose a Game", style = MaterialTheme.typography.h5)
        Button(onClick = { onSelectGame("ludo") }) { Text("Play Ludo Game") }
        Button(onClick = { onSelectGame("tank") }) { Text("Play Tank Game") }
        Button(onClick = { onSelectGame("archery") }) { Text("Play Archery Game") }
        Button(onClick = { onSelectGame("castle") }) { Text("Play Castle Game") }
    }
}

@Composable
fun GameScreen(gamePath: String, onBack: () -> Unit, activity: MainActivity) {
    Column(modifier = Modifier.fillMaxSize()) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Back")
            }
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(gamePath)
                }
            },
            modifier = Modifier.weight(1f)
        )

        AndroidView(factory = { context ->
            AdView(context).apply {
                adSize = AdSize.BANNER
                adUnitId = "ca-app-pub-8105096464664625/6118918264"
                loadAd(AdRequest.Builder().build())
            }
        })

        val nativeAd = activity.getNativeAd()
        if (nativeAd != null) {
            AndroidView(factory = { context ->
                NativeAdView(context).apply {
                    setNativeAd(nativeAd)
                }
            }, modifier = Modifier.height(120.dp).padding(8.dp))
        }
    }
}
