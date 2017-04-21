package com.yalin.style.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yalin.style.R;
import com.yalin.style.injection.component.WallpaperComponent;
import com.yalin.style.model.WallpaperItem;
import com.yalin.style.presenter.WallpaperDetailPresenter;
import com.yalin.style.util.TypefaceUtil;
import com.yalin.style.view.WallpaperDetailView;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class WallpaperDetailFragment extends BaseFragment implements WallpaperDetailView {

    @Inject
    WallpaperDetailPresenter presenter;

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
        tvAttribution = (TextView) rootView.findViewById(R.id.attribution);
        tvTitle = (TextView) rootView.findViewById(R.id.title);
        tvByline = (TextView) rootView.findViewById(R.id.byline);
        return rootView;
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
}
