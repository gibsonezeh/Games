package com.gibson.games

import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
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
        loadAppOpenAd()
        loadInterstitialAd()
        loadRewardedAd()
        loadRewardedInterstitialAd()
        loadNativeAd()

        setContent {
            GameApp(this)
        }
    }

    override fun onResume() {
        super.onResume()
        appOpenAd?.show(this)
    }

    // Ad loading functions
    fun loadAppOpenAd() {
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

    fun showInterstitialAd() {
        interstitialAd?.show(this)
        loadInterstitialAd()
    }

    fun showRewardedAd() {
        rewardedAd?.show(this) { _: RewardItem -> }
        loadRewardedAd()
    }

    fun showRewardedInterstitialAd() {
        rewardedInterstitialAd?.show(this) { _: RewardItem -> }
        loadRewardedInterstitialAd()
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

    fun loadNativeAd() {
        val builder = AdLoader.Builder(this, "ca-app-pub-8105096464664625/9250388185")
        builder.forNativeAd { ad -> nativeAd = ad }
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    nativeAd = null
                }
            })
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    fun getNativeAd(): NativeAd? = nativeAd
}

// Composables

@Composable
fun GameApp(activity: MainActivity) {
    var selectedGame by remember { mutableStateOf<String?>(null) }

    // Show ads on timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(2 * 60 * 1000)
            activity.showRewardedAd()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5 * 60 * 1000)
            activity.showRewardedInterstitialAd()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(7 * 60 * 1000)
            activity.showInterstitialAd()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10 * 60 * 1000)
            activity.loadNativeAd()
        }
    }

    if (selectedGame == null) {
        GameMenu(activity) { selectedGame = it }
    } else {
        GameScreen("file:///android_asset/games/$selectedGame/$selectedGame.html", { selectedGame = null }, activity)
    }
}

@Composable
fun GameMenu(activity: MainActivity, onSelectGame: (String) -> Unit) {
    val nativeAd = activity.getNativeAd()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Choose a Game", style = MaterialTheme.typography.h5)

        val games = listOf("ludo", "tank", "archery", "castle", "bike", "racer", "shooter", "runner")
        games.forEachIndexed { index, game ->
            Button(onClick = { onSelectGame(game) }) {
                Text("Play ${game.replaceFirstChar { it.uppercase() }} Game")
            }

            if ((index + 1) % 3 == 0 && nativeAd != null) {
                AndroidView(
                    factory = { context ->
                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.native_ad_view, null) as NativeAdView

                        adView.headlineView = adView.findViewById(R.id.ad_headline)
                        (adView.headlineView as TextView).text = nativeAd.headline
                        adView.mediaView = adView.findViewById(R.id.ad_media)
                        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                        (adView.callToActionView as Button).text = nativeAd.callToAction
                        adView.setNativeAd(nativeAd)
                        adView
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GameScreen(gamePath: String, onBack: () -> Unit, activity: MainActivity) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Back")
            }
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    addJavascriptInterface(WebAppInterface(context), "Android")
                    loadUrl(gamePath)
                }
            },
            modifier = Modifier.weight(1f)
        )

        // Banner Ad only
        AndroidView(factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-8105096464664625/6118918264"
                loadAd(AdRequest.Builder().build())
            }
        })
    }
}
