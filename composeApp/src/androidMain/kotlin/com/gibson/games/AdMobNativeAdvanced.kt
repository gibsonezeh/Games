package com.gibson.games

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun AdMobNativeAdvanced(
    adUnitId: String,
    modifier: Modifier = Modifier,
    activity: Activity // Needed for context and layout inflater
) {
    val context = LocalContext.current
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var adFailedToLoad by remember { mutableStateOf(false) }

    LaunchedEffect(adUnitId, activity) { // Use LaunchedEffect for one-time load
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad: NativeAd ->
                nativeAdState = ad
                adFailedToLoad = false
                Log.d("AdMobNativeAdvanced", "Native ad loaded: $adUnitId")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("AdMobNativeAdvanced", "Native ad failed to load: $adUnitId, Error: ${loadAdError.message}")
                    nativeAdState = null
                    adFailedToLoad = true
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAdState?.destroy()
        }
    }

    if (adFailedToLoad) {
        Box(modifier = modifier.fillMaxWidth().height(100.dp).padding(8.dp), contentAlignment = Alignment.Center) {
            Text("Native Ad: Load Failed")
        }
        return
    }

    nativeAdState?.let { ad ->
        AndroidView(
            modifier = modifier.fillMaxWidth(), // Adjust size as needed
            factory = {
                // Inflate the ad layout. Ensure you have a layout file (e.g., R.layout.ad_unified)
                // For this example, we assume a layout file named "ad_unified.xml" exists in res/layout
                // You MUST create this XML layout file in your project.
                val adView = LayoutInflater.from(it).inflate(R.layout.ad_unified, null) as NativeAdView
                populateNativeAdView(ad, adView)
                adView
            }
        )
    } ?: run {
        // Placeholder while loading or if no ad is available
        Box(modifier = modifier.fillMaxWidth().height(100.dp).padding(8.dp), contentAlignment = Alignment.Center) {
            Text("Native Ad: Loading...")
        }
    }
}

fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // Set the media view.
    adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

    // Set other ad assets.
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.priceView = adView.findViewById(R.id.ad_price)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.storeView = adView.findViewById(R.id.ad_store)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    // The headline, body, and icon are guaranteed to be in every NativeAd.
    (adView.headlineView as? TextView)?.text = nativeAd.headline
    (adView.bodyView as? TextView)?.text = nativeAd.body
    (adView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)

    // These assets aren"t guaranteed to be in every NativeAd, so check before accessing them.
    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = android.view.View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = android.view.View.VISIBLE
        (adView.callToActionView as? Button)?.text = nativeAd.callToAction
    }

    if (nativeAd.price == null) {
        adView.priceView?.visibility = android.view.View.INVISIBLE
    } else {
        adView.priceView?.visibility = android.view.View.VISIBLE
        (adView.priceView as? TextView)?.text = nativeAd.price
    }

    if (nativeAd.store == null) {
        adView.storeView?.visibility = android.view.View.INVISIBLE
    } else {
        adView.storeView?.visibility = android.view.View.VISIBLE
        (adView.storeView as? TextView)?.text = nativeAd.store
    }

    if (nativeAd.starRating == null) {
        adView.starRatingView?.visibility = android.view.View.INVISIBLE
    } else {
        (adView.starRatingView as? RatingBar)?.rating = nativeAd.starRating!!.toFloat()
        adView.starRatingView?.visibility = android.view.View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        adView.advertiserView?.visibility = android.view.View.INVISIBLE
    } else {
        (adView.advertiserView as? TextView)?.text = nativeAd.advertiser
        adView.advertiserView?.visibility = android.view.View.VISIBLE
    }

    // This method tells the Google Mobile Ads SDK that you have finished populating your
    // native ad view with this native ad.
    adView.setNativeAd(nativeAd)
}

// You need to create R.layout.ad_unified and R.id.x for this to compile.
// This is a placeholder for the actual R file generated by Android build process.
object R {
    object layout {
        const val ad_unified = 0 // Placeholder for your native ad XML layout file
    }
    object id {
        const val ad_media = 0
        const val ad_headline = 0
        const val ad_body = 0
        const val ad_call_to_action = 0
        const val ad_app_icon = 0
        const val ad_price = 0
        const val ad_stars = 0
        const val ad_store = 0
        const val ad_advertiser = 0
    }
}

