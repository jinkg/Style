package com.yalin.style.view.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yalin.style.R;
import com.yalin.style.injection.component.WallpaperComponent;
import com.yalin.style.model.WallpaperItem;
import com.yalin.style.presenter.WallpaperDetailPresenter;
import com.yalin.style.util.ScrimUtil;
import com.yalin.style.util.TypefaceUtil;
import com.yalin.style.view.WallpaperDetailView;
import com.yalin.style.view.activity.StyleActivity;
import com.yalin.style.view.component.DrawInsetsFrameLayout;
import com.yalin.style.view.component.PanScaleProxyView;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class WallpaperDetailFragment extends BaseFragment implements WallpaperDetailView,
        StyleActivity.InsetsChangeListener, View.OnSystemUiVisibilityChangeListener {

    @Inject
    WallpaperDetailPresenter presenter;

    DrawInsetsFrameLayout mainContainer;
    PanScaleProxyView panScaleProxyView;
    View statusBarScrimView;
    View chromeContainer;
    TextView tvAttribution;
    TextView tvTitle;
    TextView tvByline;

    public static WallpaperDetailFragment createInstance() {
        return new WallpaperDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent(WallpaperComponent.class).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_wallpaper_detail, container, false);
        mainContainer = (DrawInsetsFrameLayout) rootView.findViewById(R.id.container);
        panScaleProxyView = (PanScaleProxyView) rootView.findViewById(R.id.pan_scale_proxy);
        statusBarScrimView = rootView.findViewById(R.id.statusbar_scrim);
        chromeContainer = rootView.findViewById(R.id.chrome_container);
        tvAttribution = (TextView) rootView.findViewById(R.id.attribution);
        tvTitle = (TextView) rootView.findViewById(R.id.title);
        tvByline = (TextView) rootView.findViewById(R.id.byline);
        setupDetailViews();
        return rootView;
    }

    private void setupDetailViews() {
        registerInsetsChangeListener();
        chromeContainer.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
                0xaa000000, 8, Gravity.BOTTOM));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            statusBarScrimView.setVisibility(View.GONE);
            statusBarScrimView = null;
        } else {
            statusBarScrimView.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(
                    0x44000000, 8, Gravity.TOP));
        }

        panScaleProxyView.setMaxZoom(5);
        if (getActivity() instanceof PanScaleProxyView.OnOtherGestureListener) {
            panScaleProxyView.setOnOtherGestureListener(
                    (PanScaleProxyView.OnOtherGestureListener) getActivity());
        }
    }

    private void registerInsetsChangeListener() {
        if (getActivity() instanceof StyleActivity) {
            ((StyleActivity) getActivity()).registerInsetsChangeListener(this);
        }
    }

    private void unregisterInsetsChangeListener() {
        if (getActivity() instanceof StyleActivity) {
            ((StyleActivity) getActivity()).unregisterInsetsChangeListener(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterInsetsChangeListener();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        if (savedInstanceState == null) {
            loadWallpaper();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }

    private void loadWallpaper() {
        presenter.initialize();
    }

    @Override
    public void renderWallpaper(WallpaperItem wallpaperItem) {
        String titleFont = "AlegreyaSans-Black.ttf";
        String bylineFont = "AlegreyaSans-Medium.ttf";
        tvTitle.setTypeface(TypefaceUtil.getAndCache(context(), titleFont));
        tvTitle.setText(wallpaperItem.title);
        tvAttribution.setText(wallpaperItem.attribution);
        tvByline.setTypeface(TypefaceUtil.getAndCache(context(), bylineFont));
        tvByline.setText(wallpaperItem.byline);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        showToastMessage(message);
    }

    @Override
    public Context context() {
        return getActivity();
    }

    @Override
    public void onInsetsChanged(Rect insets) {
        chromeContainer.setPadding(
                insets.left, insets.top, insets.right, insets.bottom);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        final boolean visible = (visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0;

        final float metadataSlideDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        chromeContainer.setVisibility(View.VISIBLE);
        chromeContainer.animate()
                .alpha(visible ? 1f : 0f)
                .translationY(visible ? 0 : metadataSlideDistance)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (!visible) {
                            chromeContainer.setVisibility(View.GONE);
                        }
                    }
                });

        if (statusBarScrimView != null) {
            statusBarScrimView.setVisibility(View.VISIBLE);
            statusBarScrimView.animate()
                    .alpha(visible ? 1f : 0f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (!visible) {
                                statusBarScrimView.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }
}
