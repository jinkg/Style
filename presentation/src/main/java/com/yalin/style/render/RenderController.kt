package com.yalin.style.render

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Message

import com.yalin.style.data.log.LogUtil
import com.yalin.style.domain.Wallpaper
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetWallpaper
import com.yalin.style.domain.interactor.OpenWallpaperInputStream
import com.yalin.style.mapper.WallpaperItemMapper
import com.yalin.style.settings.Prefs

import java.io.InputStream

import javax.inject.Inject

/**
 * YaLin 2016/12/30.
 */

open class RenderController @Inject
constructor(protected var mContext: Context, private val getWallpaperUseCase: GetWallpaper,
            private val openWallpaperInputStreamUseCase: OpenWallpaperInputStream,
            private val wallpaperItemMapper: WallpaperItemMapper) {
    protected var mRenderer: StyleBlurRenderer? = null
    protected var mCallbacks: Callbacks? = null
    protected var mVisible: Boolean = false
    private var mQueuedBitmapRegionLoader: BitmapRegionLoader? = null
    private val wallpaperRefreshObserver: WallpaperRefreshObserver
    private val mOnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                mRenderer?.apply {
                    if (Prefs.PREF_BLUR_AMOUNT == key) {
                        recomputeMaxPrescaledBlurPixels()
                        throttledForceReloadCurrentArtwork()
                    } else if (Prefs.PREF_DIM_AMOUNT == key) {
                        recomputeMaxDimAmount()
                        throttledForceReloadCurrentArtwork()
                    } else if (Prefs.PREF_GREY_AMOUNT == key) {
                        recomputeGreyAmount()
                        throttledForceReloadCurrentArtwork()
                    }
                }
            }

    init {
        wallpaperRefreshObserver = WallpaperRefreshObserver()
        this.getWallpaperUseCase.registerObserver(wallpaperRefreshObserver)

        Prefs.getSharedPreferences(mContext)
                .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener)
    }

    fun setComponent(renderer: StyleBlurRenderer, callbacks: Callbacks) {
        this.mRenderer = renderer
        this.mCallbacks = callbacks
        reloadCurrentWallpaper()
    }

    open fun destroy() {
        if (mQueuedBitmapRegionLoader != null) {
            mQueuedBitmapRegionLoader!!.destroy()
        }
        Prefs.getSharedPreferences(mContext)
                .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener)
        getWallpaperUseCase.unregisterObserver(wallpaperRefreshObserver)
    }

    @Throws(Exception::class)
    private fun createBitmapRegionLoader(inputStream: InputStream): BitmapRegionLoader {
        val bitmapRegionLoader = BitmapRegionLoader.newInstance(inputStream) ?:
                throw IllegalStateException("Bitmap region loader create failed.")
        return bitmapRegionLoader
    }

    fun reloadCurrentWallpaper() {
        getWallpaperUseCase.execute(WallpaperItemObserver(), null)
    }

    fun setVisible(visible: Boolean) {
        mVisible = visible
        if (visible) {
            mCallbacks?.apply {
                queueEventOnGlThread(Runnable {
                    if (mQueuedBitmapRegionLoader != null) {
                        mRenderer!!.setAndConsumeBitmapRegionLoader(mQueuedBitmapRegionLoader)
                        mQueuedBitmapRegionLoader = null
                    }
                })
                requestRender()
            }
        }
    }

    private fun setBitmapRegionLoader(bitmapRegionLoader: BitmapRegionLoader) {
        mCallbacks!!.queueEventOnGlThread(Runnable {
            if (mVisible) {
                mRenderer!!.setAndConsumeBitmapRegionLoader(bitmapRegionLoader)
            } else {
                mQueuedBitmapRegionLoader = bitmapRegionLoader
            }
        })
    }

    private fun throttledForceReloadCurrentArtwork() {
        mThrottledForceReloadHandler.removeMessages(0)
        mThrottledForceReloadHandler.sendEmptyMessageDelayed(0, 250)
    }

    private val mThrottledForceReloadHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            reloadCurrentWallpaper()
        }
    }

    private inner class WallpaperItemObserver : DefaultObserver<Wallpaper>() {
        override fun onNext(wallpaper: Wallpaper) {
            val wallpaperItem = wallpaperItemMapper.transform(wallpaper)
            openWallpaperInputStreamUseCase.execute(WallpaperInputStreamObserver(),
                    OpenWallpaperInputStream.Params.openInputStream(wallpaperItem.wallpaperId))
        }

        override fun onError(exception: Throwable) {
            LogUtil.E(TAG, "Load wallpaper failed.", exception)
        }
    }


    private inner class WallpaperInputStreamObserver : DefaultObserver<InputStream>() {
        override fun onNext(inputStream: InputStream) {
            try {
                val bitmapRegionLoader = createBitmapRegionLoader(inputStream)
                LogUtil.D(TAG, "Create bitmap region loader success.")
                setBitmapRegionLoader(bitmapRegionLoader)
            } catch (e: Exception) {
                onError(e)
            }

        }

        override fun onError(exception: Throwable) {
            LogUtil.E(TAG, "Open input stream failed. ", exception)
        }
    }

    private inner class WallpaperRefreshObserver : DefaultObserver<Void>() {
        override fun onComplete() {
            LogUtil.D(TAG, "Wallpaper update,reload wallpaper.")
            reloadCurrentWallpaper()
        }
    }

    interface Callbacks {

        fun queueEventOnGlThread(runnable: Runnable)

        fun requestRender()
    }

    companion object {
        private val TAG = "RenderController"
    }
}
