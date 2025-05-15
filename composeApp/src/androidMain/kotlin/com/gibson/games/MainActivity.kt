
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
import com.google.android.gms.ads.nativead.NativeAdViewAttributes
import com.google.android.gms.ads.nativead.NativeAdViewHolder
import com.google.android.gms.ads.nativead.NativeAdViewHolderDelegate
import com.google.android.gms.ads.nativead.NativeAdViewProvider
import com.google.android.gms.ads.nativead.NativeAdViewUtils
import com.google.android.gms.ads.nativead.NativeAdLoader
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
        showAppOpenAd()
    }

    private fun loadAppOpenAd() {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(this,
            "ca-app-pub-8105096464664625/3196099815",
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                }
            })
    }

    private fun showAppOpenAd() {
        appOpenAd?.show(this)
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,
            "ca-app-pub-8105096464664625/5115702056",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    private fun showInterstitialAd() {
        interstitialAd?.show(this)
        loadInterstitialAd()
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this,
            "ca-app-pub-8105096464664625/6717684064",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    private fun showRewardedAd() {
        rewardedAd?.show(this) { _: RewardItem -> }
        loadRewardedAd()
    }

    private fun loadRewardedInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(this,
            "ca-app-pub-8105096464664625/9695201197",
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                }
            })
    }

    private fun showRewardedInterstitialAd() {
        rewardedInterstitialAd?.show(this) { _: RewardItem -> }
        loadRewardedInterstitialAd()
    }

    private fun loadNativeAd() {
        val builder = AdLoader.Builder(this, "ca-app-pub-8105096464664625/9250388185")
        builder.forNativeAd { ad: NativeAd ->
            nativeAd = ad
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                nativeAd = null
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun getNativeAd(): NativeAd? = nativeAd
}

// Composables

@Composable
fun GameApp(activity: MainActivity) {
    var selectedGame by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2 * 60 * 1000) // 2 min
            activity.showRewardedAd()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5 * 60 * 1000) // 5 min
            activity.showRewardedInterstitialAd()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(7 * 60 * 1000) // 7 min
            activity.showInterstitialAd()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10 * 60 * 1000) // 10 min
            activity.loadNativeAd()
        }
    }

    if (selectedGame == null) {
        GameMenu { selectedGame = it }
    } else {
        GameScreen(
            gamePath = "file:///android_asset/games/$selectedGame/$selectedGame.html",
            onBack = { selectedGame = null },
            activity = activity
        )
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

        // Banner Ad
        AndroidView(factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-8105096464664625/6118918264"
                loadAd(AdRequest.Builder().build())
            }
        })

        // Native Advanced Ad
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
