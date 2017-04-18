package com.yalin.style.render;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;

/**
 * YaLin 2016/12/30.
 */

public class DemoRenderController extends RenderController {

  private static final String TAG = "DemoRenderController";

  private final Handler mHandler = new Handler();

  private static final long ANIMATION_CYCLE_TIME_MILLIS = 35000;
  private static final long FOCUS_DELAY_TIME_MILLIS = 2000;
  private static final long FOCUS_TIME_MILLIS = 6000;

  private Animator mCurrentScrollAnimator;
  private boolean mReverseDirection = false;
  private boolean mAllowFocus = true;


  public DemoRenderController(Context context, StyleBlurRenderer renderer,
      Callbacks callbacks, boolean allowFocus) {
    super(context, renderer, callbacks);
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
    }
    mHandler.removeCallbacksAndMessages(null);
  }

  @Override
  protected BitmapRegionLoader openDownloadedCurrentArtwork(boolean forceReload) {
    try {
      return BitmapRegionLoader.newInstance(mContext.getAssets().open("starrynight.jpg"));
    } catch (IOException e) {
      Log.e(TAG, "Error opening demo image.", e);
      return null;
    }
  }

}
