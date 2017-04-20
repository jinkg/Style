package com.yalin.style.view.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import com.yalin.style.R;

/**
 * YaLin 2017/1/4.
 */

public class AnimatedStyleLogoFragment extends Fragment {

  private Runnable mOnFillStartedCallback;
  private View mSubtitleView;
  private ImageView mLogoView;
  private float mInitialLogoOffset;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mInitialLogoOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
        getResources().getDisplayMetrics());
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.animated_logo_fragment, container, false);
    mSubtitleView = rootView.findViewById(R.id.logo_subtitle);

    mLogoView = (ImageView) rootView.findViewById(R.id.logo_view);
    reset();
    return rootView;
  }

  public void start() {
    mLogoView.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        mSubtitleView.setVisibility(View.VISIBLE);
        mSubtitleView.setTranslationY(-mSubtitleView.getHeight());
        Interpolator interpolator = new OvershootInterpolator();
        mSubtitleView.animate().translationY(0).setInterpolator(interpolator).setDuration(500)
            .start();

        if (mOnFillStartedCallback != null) {
          mOnFillStartedCallback.run();
        }

      }
    }).setDuration(500).start();

  }

  public void setOnFillStartedCallback(Runnable fillStartedCallback) {
    mOnFillStartedCallback = fillStartedCallback;
  }

  public void reset() {
    mLogoView.setTranslationY(mInitialLogoOffset);
    mSubtitleView.setVisibility(View.INVISIBLE);
  }
}
