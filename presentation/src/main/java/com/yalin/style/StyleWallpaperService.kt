package com.yalin.style

import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.support.v4.os.UserManagerCompat
import android.support.v4.view.GestureDetectorCompat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.ViewConfiguration

import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.event.SystemWallpaperSizeChangedEvent
import com.yalin.style.event.WallpaperActivateEvent
import com.yalin.style.event.WallpaperDetailOpenedEvent
import com.yalin.style.event.WallpaperSwitchEvent
import com.yalin.style.render.RenderController
import com.yalin.style.render.StyleBlurRenderer
import com.yalin.style.settings.Prefs

import net.rbgrn.android.glwallpaperservice.GLWallpaperService

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

/**
 * YaLin 2016/12/30.
 */
class StyleWallpaperService : GLWallpaperService() {

    companion object {
        private val TAG = "StyleWallpaperService"

        private val TEMPORARY_FOCUS_DURATION_MILLIS: Long = 3000
    }

    private var mInitialized = false
    private var mUnlockReceiver: BroadcastReceiver? = null

    override fun onCreateEngine(): WallpaperService.Engine {
        return StyleWallpaperEngine()
    }

    override fun onCreate() {
        super.onCreate()

        if (UserManagerCompat.isUserUnlocked(this)) {
            initialize()
        } else if (VERSION.SDK_INT >= 24) {
            mUnlockReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    initialize()
                    unregisterReceiver(this)
                }
            }
            val filter = IntentFilter(Intent.ACTION_USER_UNLOCKED)
            registerReceiver(mUnlockReceiver, filter)
        }
    }

    private fun initialize() {

        mInitialized = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mInitialized) {
            //todo
        } else {
            unregisterReceiver(mUnlockReceiver)
        }
    }

    inner class StyleWallpaperEngine : GLWallpaperService.GLEngine(),
            StyleBlurRenderer.Callbacks, RenderController.Callbacks {

        private var mRenderer: StyleBlurRenderer? = null

        @Inject
        lateinit var mRenderController: RenderController

        internal val mGestureDetector: GestureDetectorCompat =
                GestureDetectorCompat(this@StyleWallpaperService,
                        object : GestureDetector.SimpleOnGestureListener() {
                            override fun onDown(e: MotionEvent): Boolean {
                                return true
                            }

                            override fun onDoubleTap(e: MotionEvent): Boolean {
                                if (mWallpaperDetailMode) {
                                    return true
                                }

                                mValidDoubleTap = true
                                mMainThreadHandler.removeCallbacks(mDoubleTapTimeoutRunnable)
                                val timeout = ViewConfiguration.getTapTimeout()
                                mMainThreadHandler.postDelayed(mDoubleTapTimeoutRunnable,
                                        timeout.toLong())
                                return true
                            }
                        })

        private val mMainThreadHandler = Handler()

        private var mVisible = true

        // is MainActivity visible
        private var mWallpaperDetailMode = false

        // is last double tab valid
        private var mValidDoubleTap = false

        private var mEngineUnlockReceiver: BroadcastReceiver? = null

        private var mWallpaperActivate = false

        private var mIsLockScreenVisibleReceiverRegistered = false
        private val mLockScreenPreferenceChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                    if (Prefs.PREF_DISABLE_BLUR_WHEN_LOCKED == key) {
                        if (sp.getBoolean(Prefs.PREF_DISABLE_BLUR_WHEN_LOCKED, false)) {
                            val intentFilter = IntentFilter()
                            intentFilter.addAction(Intent.ACTION_USER_PRESENT)
                            intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
                            intentFilter.addAction(Intent.ACTION_SCREEN_ON)
                            registerReceiver(mLockScreenVisibleReceiver, intentFilter)
                            mIsLockScreenVisibleReceiverRegistered = true
                            // If the user is not yet unlocked (i.e., using Direct Boot), we should
                            // immediately send the lock screen visible callback
                            if (!UserManagerCompat.isUserUnlocked(this@StyleWallpaperService)) {
                                lockScreenVisibleChanged(true)
                            }
                        } else if (mIsLockScreenVisibleReceiverRegistered) {
                            unregisterReceiver(mLockScreenVisibleReceiver)
                            mIsLockScreenVisibleReceiverRegistered = false
                        }
                    }
                }
        private val mLockScreenVisibleReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent != null) {
                    if (Intent.ACTION_USER_PRESENT == intent.action) {
                        lockScreenVisibleChanged(false)
                    } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                        lockScreenVisibleChanged(true)
                    } else if (Intent.ACTION_SCREEN_ON == intent.action) {
                        val kgm = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if (!kgm.inKeyguardRestrictedInputMode()) {
                            lockScreenVisibleChanged(false)
                        }
                    }
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            mRenderer = StyleBlurRenderer(this@StyleWallpaperService, this).apply {
                setIsPreview(isPreview)
            }

            setEGLContextClientVersion(2)
            setEGLConfigChooser(8, 8, 8, 0, 0, 0)
            setRenderer(mRenderer)
            renderMode = GLWallpaperService.GLEngine.RENDERMODE_WHEN_DIRTY
            requestRender()

            StyleApplication.instance.applicationComponent.inject(this)

            mRenderController.setComponent(mRenderer!!, this)

            val sp = Prefs.getSharedPreferences(this@StyleWallpaperService)
            sp.registerOnSharedPreferenceChangeListener(mLockScreenPreferenceChangeListener)
            // Trigger the initial registration if needed
            mLockScreenPreferenceChangeListener.onSharedPreferenceChanged(sp,
                    Prefs.PREF_DISABLE_BLUR_WHEN_LOCKED)

            if (!isPreview) {
                if (UserManagerCompat.isUserUnlocked(applicationContext)) {
                    activateWallpaper()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mEngineUnlockReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            activateWallpaper()
                            unregisterReceiver(this)
                        }
                    }
                    val filter = IntentFilter(Intent.ACTION_USER_UNLOCKED)
                    registerReceiver(mEngineUnlockReceiver, filter)
                }
            }

            setTouchEventsEnabled(true)
            setOffsetNotificationsEnabled(true)
            EventBus.getDefault().register(this)
        }

        override fun onDestroy() {
            EventBus.getDefault().unregister(this)
            deactivateWallpaper()
            if (mIsLockScreenVisibleReceiverRegistered) {
                unregisterReceiver(mLockScreenVisibleReceiver)
            }
            Prefs.getSharedPreferences(this@StyleWallpaperService)
                    .unregisterOnSharedPreferenceChangeListener(
                            mLockScreenPreferenceChangeListener)
            queueEvent {
                mRenderer?.destroy()
            }
            mRenderController.destroy()
            super.onDestroy()
        }

        private fun activateWallpaper() {
            mWallpaperActivate = true
            Analytics.logEvent(this@StyleWallpaperService, Event.WALLPAPER_CREATED)
            EventBus.getDefault().postSticky(WallpaperActivateEvent(true))
        }

        private fun deactivateWallpaper() {
            if (mWallpaperActivate) {
                Analytics.logEvent(this@StyleWallpaperService, Event.WALLPAPER_DESTROYED)
                EventBus.getDefault().postSticky(WallpaperActivateEvent(false))
            }
        }

        @Subscribe
        fun onEventMainThread(e: WallpaperDetailOpenedEvent) {
            if (e.isWallpaperDetailOpened == mWallpaperDetailMode) {
                return
            }

            mWallpaperDetailMode = e.isWallpaperDetailOpened
            cancelDelayedBlur()
            queueEvent { mRenderer?.setIsBlurred(!e.isWallpaperDetailOpened, true) }
        }

        @Subscribe
        fun onEventMainThread(e: WallpaperDetailViewport) {
            requestRender()
        }

        @Subscribe
        fun onEventMainThread(e: WallpaperSwitchEvent) {
            mRenderController.reloadCurrentWallpaper()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            if (!isPreview) {
                EventBus.getDefault().postSticky(
                        SystemWallpaperSizeChangedEvent(width, height))
            }
            mRenderController.reloadCurrentWallpaper()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            mVisible = visible
            mRenderController.setVisible(visible)
        }

        override fun onOffsetsChanged(xOffset: Float, yOffset: Float, xOffsetStep: Float,
                                      yOffsetStep: Float, xPixelOffset: Int, yPixelOffset: Int) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep,
                    yOffsetStep, xPixelOffset, yPixelOffset)
            mRenderer?.setNormalOffsetX(xOffset)
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            mGestureDetector.onTouchEvent(event)
            delayBlur()
        }

        override fun onCommand(action: String, x: Int, y: Int, z: Int, extras: Bundle?,
                               resultRequested: Boolean): Bundle? {
            if (WallpaperManager.COMMAND_TAP == action && mValidDoubleTap) {
                queueEvent {
                    mRenderer?.setIsBlurred(!mRenderer!!.isBlurred, false)
                    delayBlur()
                }
                mValidDoubleTap = false
            }
            return super.onCommand(action, x, y, z, extras, resultRequested)
        }

        override fun queueEventOnGlThread(runnable: Runnable) {
            queueEvent(runnable)
        }

        override fun requestRender() {
            if (mVisible) {
                super.requestRender()
            }
        }

        private fun lockScreenVisibleChanged(isLockScreenVisible: Boolean) {
            cancelDelayedBlur()
            queueEvent { mRenderer?.setIsBlurred(!isLockScreenVisible, false) }
        }

        private fun cancelDelayedBlur() {
            mMainThreadHandler.removeCallbacks(mBlurRunnable)
        }

        private fun delayBlur() {
            if (mWallpaperDetailMode || mRenderer!!.isBlurred) {
                return
            }
            cancelDelayedBlur()
            mMainThreadHandler.postDelayed(mBlurRunnable, TEMPORARY_FOCUS_DURATION_MILLIS)
        }

        private val mBlurRunnable = Runnable {
            queueEvent {
                mRenderer?.setIsBlurred(true, false)
            }
        }

        private val mDoubleTapTimeoutRunnable = Runnable { mValidDoubleTap = false }
    }
}
