package com.yalin.style.analytics

import android.content.Context

import com.flurry.android.FlurryAgent
import com.yalin.style.BuildConfig
import com.yalin.style.data.log.LogUtil

import java.util.HashMap

/**
 * @author jinyalin
 * *
 * @since 2017/5/3.
 */

object Analytics {
    fun init(context: Context) {
        FlurryAgent.Builder().withLogEnabled(LogUtil.LOG_ENABLE)
                .build(context, BuildConfig.FLURRY_API_KEY)
    }


    fun setUserProperty(context: Context, key: String, value: String) {

    }

    fun onStartSession(context: Context) {
        FlurryAgent.onStartSession(context)
    }

    fun onEndSession(context: Context) {
        FlurryAgent.onEndSession(context)
    }

    fun logEvent(context: Context, event: String) {
        FlurryAgent.logEvent(event)
    }

    fun logEvent(context: Context, event: String, vararg params: String) {
        val paramsMap = HashMap<String, String>()
        paramsMap.put(event, params[0])
        FlurryAgent.logEvent(event, paramsMap)
    }
}
