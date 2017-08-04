package com.yalin.style.analytics

import android.content.Context
import android.os.Bundle

import com.flurry.android.FlurryAgent
import com.yalin.style.BuildConfig
import com.yalin.style.data.log.LogUtil

import java.util.HashMap

/**
 * @author jinyalin
 * *
 * @since 2017/5/3.
 */

object Analytics : IAnalytics {
    override fun init(context: Context) {
        FlurryAgent.Builder().withLogEnabled(LogUtil.LOG_ENABLE)
                .build(context, BuildConfig.FLURRY_API_KEY)
    }


    override fun setUserProperty(context: Context, key: String, value: String) {
    }

    override fun onStartSession(context: Context) {
        FlurryAgent.onStartSession(context)
    }

    override fun onEndSession(context: Context) {
        FlurryAgent.onEndSession(context)
    }

    override fun logEvent(context: Context, event: String) {
        FlurryAgent.logEvent(event)
    }

    override fun logEvent(context: Context, event: String, vararg params: String) {
        val paramsMap = HashMap<String, String>()
        paramsMap.put(event, params[0])
        FlurryAgent.logEvent(event, paramsMap)
    }
}
