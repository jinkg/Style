package com.yalin.style.engine


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.support.v4.os.UserManagerCompat
import android.view.SurfaceHolder

import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService

/**
 * @author jinyalin
 * *
 * @since 2017/7/28.
 */

open class GDXWallpaperServiceProxy(host: Context) : AndroidLiveWallpaperService() {
    private val activateCallback: WallpaperActiveCallback?

    init {
        @Suppress("LeakingThis")
        attachBaseContext(host)

        activateCallback = if (host is WallpaperActiveCallback) host else null
    }

    override fun onCreateEngine(): WallpaperService.Engine {
        return GDXActiveEngine()
    }

    inner class GDXActiveEngine : AndroidWallpaperEngine() {
        private var mWallpaperActivate = false

        private var mEngineUnlockReceiver: BroadcastReceiver? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            if (!isPreview) {
                if (UserManagerCompat.isUserUnlocked(this@GDXWallpaperServiceProxy)) {
                    activateWallpaper()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mEngineUnlockReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            activateWallpaper()
                            context.unregisterReceiver(this)
                        }
                    }
                    val filter = IntentFilter(Intent.ACTION_USER_UNLOCKED)
                    this@GDXWallpaperServiceProxy.registerReceiver(mEngineUnlockReceiver, filter)
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            deactivateWallpaper()
        }

        private fun activateWallpaper() {
            mWallpaperActivate = true
            activateCallback?.onWallpaperActivate()
        }

        private fun deactivateWallpaper() {
            if (mWallpaperActivate) {
                activateCallback?.onWallpaperDeactivate()
            }
        }
    }
}
