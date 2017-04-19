package com.yalin.style.render;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;

import com.yalin.style.domain.interactor.GetWallpaper;
import com.yalin.style.mapper.WallpaperItemMapper;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/4/19.
 */
public class DemoRenderController extends RenderController {
    private final Handler mHandler = new Handler();

    private static final long ANIMATION_CYCLE_TIME_MILLIS = 35000;
    private static final long FOCUS_DELAY_TIME_MILLIS = 2000;
    private static final long FOCUS_TIME_MILLIS = 6000;

    private Animator mCurrentScrollAnimator;
    private boolean mReverseDirection = false;
    private boolean mAllowFocus = false;

    @Inject
    public DemoRenderController(Context context, GetWallpaper getWallpaperUseCase,
                                WallpaperItemMapper wallpaperItemMapper) {
        super(context, getWallpaperUseCase, wallpaperItemMapper);
    }

    public void start(boolean allowFocus) {
        mAllowFocus = allowFocus;
        runAnimation();
    }

    private void runAnimation() {
        if (mCurrentScrollAnimator != null) {
            mCurrentScrollAnimator.cancel();
        }

        mCurrentScrollAnimator = ObjectAnimator
                .ofFloat(mRenderer, "normalOffsetX",
                        mReverseDirection ? 1f : 0f, mReverseDirection ? 0f : 1f)
                .setDuration(ANIMATION_CYCLE_TIME_MILLIS);
        mCurrentScrollAnimator.start();
        mCurrentScrollAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mReverseDirection = !mReverseDirection;
                runAnimation();
            }
        });
        if (mAllowFocus) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRenderer.setIsBlurred(false, false);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRenderer.setIsBlurred(true, false);
                        }
                    }, FOCUS_TIME_MILLIS);
                }
            }, FOCUS_DELAY_TIME_MILLIS);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mCurrentScrollAnimator != null) {
            mCurrentScrollAnimator.cancel();
            mCurrentScrollAnimator.removeAllListeners();
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
