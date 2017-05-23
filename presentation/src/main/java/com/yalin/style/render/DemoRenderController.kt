package com.yalin.style.render

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler

import com.yalin.style.domain.interactor.GetWallpaper
import com.yalin.style.domain.interactor.ObserverWallpaper
import com.yalin.style.domain.interactor.OpenWallpaperInputStream
import com.yalin.style.mapper.WallpaperItemMapper

import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/4/19.
 */
class DemoRenderController @Inject
constructor(context: Context, getWallpaperUseCase: GetWallpaper,
            observerWallpaper: ObserverWallpaper,
            openWallpaperInputStream: OpenWallpaperInputStream,
            wallpaperItemMapper: WallpaperItemMapper)
    : RenderController(context, getWallpaperUseCase, observerWallpaper,
        openWallpaperInputStream, wallpaperItemMapper) {

    companion object {

        private val ANIMATION_CYCLE_TIME_MILLIS: Long = 35000
        private val FOCUS_DELAY_TIME_MILLIS: Long = 2000
        private val FOCUS_TIME_MILLIS: Long = 6000

        private val mHandler = Handler()
    }

    private var mCurrentScrollAnimator: Animator? = null
    private var mReverseDirection = false
    private var mAllowFocus = false

    fun start(allowFocus: Boolean) {
        mAllowFocus = allowFocus
        runAnimation()
    }

    private fun runAnimation() {
        if (mCurrentScrollAnimator != null) {
            mCurrentScrollAnimator!!.cancel()
        }

        mCurrentScrollAnimator = ObjectAnimator
                .ofFloat(mRenderer, "normalOffsetX",
                        if (mReverseDirection) 1f else 0f, if (mReverseDirection) 0f else 1f)
                .setDuration(ANIMATION_CYCLE_TIME_MILLIS)
        mCurrentScrollAnimator!!.start()
        mCurrentScrollAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mReverseDirection = !mReverseDirection
                runAnimation()
            }
        })
        if (mAllowFocus) {
            mHandler.postDelayed({
                mRenderer!!.setIsBlurred(false, false)
                mHandler.postDelayed({ mRenderer!!.setIsBlurred(true, false) }, FOCUS_TIME_MILLIS)
            }, FOCUS_DELAY_TIME_MILLIS)
        }
    }

    override fun destroy() {
        super.destroy()
        if (mCurrentScrollAnimator != null) {
            mCurrentScrollAnimator!!.cancel()
            mCurrentScrollAnimator!!.removeAllListeners()
        }
        mHandler.removeCallbacksAndMessages(null)
    }

}
