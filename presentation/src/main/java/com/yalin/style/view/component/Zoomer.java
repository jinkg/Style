package com.yalin.style.view.component;

import android.content.Context;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.yalin.style.util.MathUtil;

/**
 * @author jinyalin
 * @since 2017/4/21.
 * A simple class that animates double-touch zoom gestures. Functionally similar to a {@link
 * android.widget.Scroller}.
 */
class Zoomer {
    /**
     * The interpolator, used for making zooms animate 'naturally.'
     */
    private Interpolator mInterpolator;

    /**
     * The total animation duration for a zoom.
     */
    private int mAnimationDurationMillis;

    /**
     * Whether or not the current zoom has finished.
     */
    private boolean mFinished = true;

    /**
     * The starting zoom value.
     */
    private float mStartZoom = 1f;

    /**
     * The current zoom value; computed by {@link #computeZoom()}.
     */
    private float mCurrentZoom;

    /**
     * The time the zoom started, computed using {@link android.os.SystemClock#elapsedRealtime()}.
     */
    private long mStartRTC;

    /**
     * The destination zoom factor.
     */
    private float mEndZoom;

    public Zoomer(Context context) {
        mInterpolator = new DecelerateInterpolator();
        mAnimationDurationMillis = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    /**
     * Forces the zoom finished state to the given value. Unlike {@link #abortAnimation()}, the
     * current zoom value isn't set to the ending value.
     *
     * @see android.widget.Scroller#forceFinished(boolean)
     */
    public void forceFinished(boolean finished) {
        mFinished = finished;
    }

    /**
     * Aborts the animation, setting the current zoom value to the ending value.
     *
     * @see android.widget.Scroller#abortAnimation()
     */
    public void abortAnimation() {
        mFinished = true;
        mCurrentZoom = mEndZoom;
    }

    /**
     * Starts a zoom from startZoom to endZoom. That is, to zoom from 100% to 125%, endZoom should
     * by 0.25f.
     *
     * @see android.widget.Scroller#startScroll(int, int, int, int)
     */
    public void startZoom(float startZoom, float endZoom) {
        mStartRTC = SystemClock.elapsedRealtime();
        mEndZoom = endZoom;

        mFinished = false;
        mStartZoom = startZoom;
        mCurrentZoom = startZoom;
    }

    /**
     * Computes the current zoom level, returning true if the zoom is still active and false if the
     * zoom has finished.
     *
     * @see android.widget.Scroller#computeScrollOffset()
     */
    public boolean computeZoom() {
        if (mFinished) {
            return false;
        }

        long tRTC = SystemClock.elapsedRealtime() - mStartRTC;
        if (tRTC >= mAnimationDurationMillis) {
            mFinished = true;
            mCurrentZoom = mEndZoom;
            return false;
        }

        float t = tRTC * 1f / mAnimationDurationMillis;
        mCurrentZoom = MathUtil.interpolate(
                mStartZoom, mEndZoom, mInterpolator.getInterpolation(t));
        return true;
    }

    /**
     * Returns the current zoom level.
     *
     * @see android.widget.Scroller#getCurrX()
     */
    public float getCurrZoom() {
        return mCurrentZoom;
    }
}
