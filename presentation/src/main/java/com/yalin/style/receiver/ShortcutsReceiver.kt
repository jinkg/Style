package com.yalin.style.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yalin.style.data.log.LogUtil

/**
 * @author jinyalin
 * @since 2017/7/11.
 */
class ShortcutsReceiver : BroadcastReceiver() {
    companion object {
        val TAG = "ShortcutsReceiver"
        val SWITCH_WALLPAPER_ACTION = "com.yalin.style.ACTION_SWITCH_WALLPAPER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (SWITCH_WALLPAPER_ACTION == action) {
            LogUtil.D(TAG, "Received shortcuts switch action.")
        }
    }
}