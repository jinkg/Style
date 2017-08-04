package com.yalin.style.analytics

import android.content.Context

/**
 * @author jinyalin
 * *
 * @since 2017/8/4.
 */

interface IAnalytics {
    fun init(context: Context)

    fun setUserProperty(context: Context, key: String, value: String)

    fun onStartSession(context: Context)

    fun onEndSession(context: Context)

    fun logEvent(context: Context, event: String)

    fun logEvent(context: Context, event: String, vararg params: String)
}
