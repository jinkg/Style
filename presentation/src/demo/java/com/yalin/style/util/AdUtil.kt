package com.yalin.style.util

import android.app.Activity
import com.google.android.gms.ads.*
import com.yalin.style.R
import com.yalin.style.view.activity.AdvanceSettingActivity

/**
 * @author jinyalin
 * @since 2017/8/10.
 */

fun maybeAttachAd(activity: Activity) {
    MobileAds.initialize(activity.applicationContext,
            activity.getString(R.string.app_ad_id))
    val adView = activity.findViewById(R.id.adView) as AdView
    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)
}

private var mInterstitialAd: InterstitialAd? = null

fun maybeAttachInterstitialAd(activity: AdvanceSettingActivity, listener: AdListener) {
    mInterstitialAd = InterstitialAd(activity)
    mInterstitialAd!!.adUnitId = activity.getString(R.string.advance_insert_ad_unit_id)
    mInterstitialAd!!.adListener = object : AdListener() {
        override fun onAdLeftApplication() {
            listener.onAdLeftApplication()
        }

        override fun onAdFailedToLoad(p0: Int) {
            listener.onAdFailedToLoad(p0)
        }

        override fun onAdClosed() {
            mInterstitialAd!!.loadAd(AdRequest.Builder().build())
            listener.onAdClosed()
        }

        override fun onAdOpened() {
            listener.onAdOpened()
        }

        override fun onAdLoaded() {
            listener.onAdLoaded()
        }
    }
    mInterstitialAd!!.loadAd(AdRequest.Builder().build())
}

fun maybeShowInterstitialAd(): Boolean {
    if (mInterstitialAd != null) {
        if (mInterstitialAd!!.isLoaded) {
            mInterstitialAd!!.show()
            return true
        }
    }
    return false
}