package com.yalin.style.analytics

import android.content.Context

/**
 * @author jinyalin
 * *
 * @since 2017/5/3.
 */

object Analytics : IAnalytics {
    override fun init(context: Context) {

    }


    override fun setUserProperty(context: Context, key: String, value: String) {

    }

    override fun onStartSession(context: Context) {
    }

    override fun onEndSession(context: Context) {
    }

    override fun logEvent(context: Context, event: String) {
    }

    override fun logEvent(context: Context, event: String, vararg params: String) {
    }
}
