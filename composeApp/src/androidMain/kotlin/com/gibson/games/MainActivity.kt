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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : ComponentActivity() {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var appOpenAd: AppOpenAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this)

        // Load all ads
        loadInterstitialAd()
        loadRewardedAd()
        loadAppOpenAd()

        setContent {
            GameApp(this)
        }
    }

    override fun onResume() {
        super.onResume()
        showAppOpenAd()
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

    fun showInterstitialAd() {
        interstitialAd?.show(this)
    }

    fun showRewardedAd() {
        rewardedAd?.show(this) { _: RewardItem ->
            // Reward logic (optional)
        }
    }
}

// Composables

@Composable
fun GameApp(activity: MainActivity) {
    var selectedGame by remember { mutableStateOf<String?>(null) }

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
        Button(onClick = { onSelectGame("ludo") }) {
            Text("Play ludo Game")
        }
         Button(onClick = { onSelectGame("tank") }) {
             Text("Play tank Game")
         }

        Button(onClick = { onSelectGame("archery") }) {
            Text("Play Archery Game")
        }
        Button(onClick = { onSelectGame("castle") }) {
            Text("Play Castle Game")
        }
    }
}

@Composable
fun GameScreen(gamePath: String, onBack: () -> Unit, activity: MainActivity) {
    Column(modifier = Modifier.fillMaxSize()) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Back")
            }
            Button(onClick = { activity.showInterstitialAd() }, modifier = Modifier.padding(8.dp)) {
                Text("Show Interstitial")
            }
            Button(onClick = { activity.showRewardedAd() }, modifier = Modifier.padding(8.dp)) {
                Text("Show Reward")
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
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-8105096464664625/6118918264"
                loadAd(AdRequest.Builder().build())
            }
        })
    }
}
