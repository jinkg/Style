package com.yalin.style.util

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.yalin.style.R

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