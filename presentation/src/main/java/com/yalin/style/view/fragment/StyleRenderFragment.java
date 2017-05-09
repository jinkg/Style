package com.yalin.style.view.fragment;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.yalin.style.StyleApplication;
import com.yalin.style.render.DemoRenderController;
import com.yalin.style.render.GLTextureView;
import com.yalin.style.render.ImageBlurrer;
import com.yalin.style.render.RenderController;
import com.yalin.style.render.StyleBlurRenderer;
import com.yalin.style.util.ImageLoader;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/4/19.
 */

public class StyleRenderFragment extends Fragment implements RenderController.Callbacks,
        StyleBlurRenderer.Callbacks {

    private static final String ARG_DEMO_MODE = "demo_mode";
    private static final String ARG_DEMO_FOCUS = "demo_focus";

    private boolean mDemoMode = false;
    private boolean mDemoFocus = false;

    private StyleView mView;
    private ImageView mSimpleDemoModeImageView;

    private ImageLoader mImageLoader;

    public static StyleRenderFragment createInstance(boolean demoMode, boolean demoFocus) {
        StyleRenderFragment fragment = new StyleRenderFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DEMO_MODE, demoMode);
        args.putBoolean(ARG_DEMO_FOCUS, demoFocus);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle args = getArguments();
            mDemoMode = args.getBoolean(ARG_DEMO_MODE);
            mDemoFocus = args.getBoolean(ARG_DEMO_FOCUS);
        } else {
            mDemoMode = savedInstanceState.getBoolean(ARG_DEMO_MODE);
            mDemoFocus = savedInstanceState.getBoolean(ARG_DEMO_FOCUS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ARG_DEMO_MODE, mDemoMode);
        outState.putBoolean(ARG_DEMO_FOCUS, mDemoFocus);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        ActivityManager activityManager = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if (mDemoMode && activityManager.isLowRamDevice()) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int targetWidth = dm.widthPixels;
            int targetHeight = dm.heightPixels;

            mSimpleDemoModeImageView = new ImageView(getActivity());
            mSimpleDemoModeImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            mImageLoader = new ImageLoader(getActivity());
            mImageLoader.beginImageLoad("file:///android_asset/painterly-architectonic.jpg",
                    null, true)
                    .override(targetWidth, targetHeight)
                    .into(mSimpleDemoModeLoadTarget);
            return mSimpleDemoModeImageView;
        } else {
            mView = new StyleView(getActivity());
            mView.setPreserveEGLContextOnPause(true);
            return mView;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mView != null) {
            mView.mRenderController.setVisible(!hidden);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mView = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mView != null) {
            mView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mView != null) {
            mView.onPause();
        }
    }

    @Override
    public void queueEventOnGlThread(Runnable runnable) {
        if (mView != null) {
            mView.queueEvent(runnable);
        }
    }

    @Override
    public void requestRender() {
        if (mView != null) {
            mView.requestRender();
        }
    }

    private Target<Bitmap> mSimpleDemoModeLoadTarget = new BaseTarget<Bitmap>() {

        @Override
        public void onResourceReady(Bitmap resource,
                                    GlideAnimation<? super Bitmap> glideAnimation) {
            if (!mDemoFocus) {
                ImageBlurrer imageBlurrer = new ImageBlurrer(getActivity(), resource);
                Bitmap blurred = imageBlurrer.blurBitmap(ImageBlurrer.MAX_SUPPORTED_BLUR_PIXELS, 0);
                imageBlurrer.destroy();

                Canvas c = new Canvas(blurred);
                c.drawColor(Color.argb(255 - StyleBlurRenderer.DEFAULT_MAX_DIM,
                        0, 0, 0));

                resource = blurred;
            }
            mSimpleDemoModeImageView.setImageBitmap(resource);
        }

        @Override
        public void getSize(SizeReadyCallback cb) {

        }
    };

    public class StyleView extends GLTextureView {
        @Inject
        DemoRenderController mRenderController;

        private StyleBlurRenderer mRenderer;

        public StyleView(Context context) {
            super(context);
            mRenderer = new StyleBlurRenderer(context, StyleRenderFragment.this);
            setEGLContextClientVersion(2);
            setEGLConfigChooser(8, 8, 8, 8, 0, 0);
            setRenderer(mRenderer);
            setRenderMode(RENDERMODE_WHEN_DIRTY);

            mRenderer.setDemoMode(mDemoMode);

            StyleApplication.Companion.getInstance().getApplicationComponent().inject(this);
            mRenderController.setComponent(mRenderer, StyleRenderFragment.this);

            mRenderController.setVisible(true);

            mRenderController.start(mDemoFocus);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mRenderer.hintViewportSize(w, h);
            mRenderController.reloadCurrentWallpaper();
        }

        @Override
        protected void onDetachedFromWindow() {
            mRenderController.destroy();
            queueEventOnGlThread(new Runnable() {
                @Override
                public void run() {
                    mRenderer.destroy();
                }
            });
            super.onDetachedFromWindow();
        }
    }

}
