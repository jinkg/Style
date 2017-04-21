package com.yalin.style.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.yalin.style.R;
import com.yalin.style.StyleWallpaperService;
import com.yalin.style.injection.HasComponent;
import com.yalin.style.injection.component.DaggerWallpaperComponent;
import com.yalin.style.injection.component.WallpaperComponent;
import com.yalin.style.view.component.DrawInsetsFrameLayout;
import com.yalin.style.view.component.PanScaleProxyView;
import com.yalin.style.view.fragment.AnimatedStyleLogoFragment;
import com.yalin.style.view.fragment.StyleRenderFragment;
import com.yalin.style.util.StyleConfig;
import com.yalin.style.view.fragment.WallpaperDetailFragment;

public class StyleActivity extends BaseActivity implements OnClickListener,
        StyleConfig.ActivateListener, HasComponent<WallpaperComponent>,
        PanScaleProxyView.OnOtherGestureListener {
    // ui mode
    private static final int MODE_UNKNOWN = -1;
    private static final int MODE_ACTIVATE = 0;
    private static final int MODE_DETAIL = 1;

    private DrawInsetsFrameLayout mMainContainer;
    private View mActiveContainer;
    private View mDetailContainer;
    private Button mActiveButton;

    private Handler mHandler = new Handler();

    private boolean mStyleActive = false;

    private WallpaperComponent wallpaperComponent;

    private int mUiMode = MODE_UNKNOWN;
    private boolean needUpdateUi = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeInjector();

        setContentView(R.layout.activity_main);

        setUpActiveView();
        setUpDetailView();

        mMainContainer = (DrawInsetsFrameLayout) findViewById(R.id.main_container);
        mMainContainer.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mDetailContainer.setPadding(
                        insets.left, insets.top, insets.right, insets.bottom);
            }
        });
        showHideChrome(true);

        mStyleActive = StyleConfig.isStyleActive();

        StyleConfig.registerActivateListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateUiIfNeed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StyleConfig.unregisterActivateListener(this);
    }

    private void showHideChrome(boolean show) {
        int flags = show ? 0 : View.SYSTEM_UI_FLAG_LOW_PROFILE;
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!show) {
            flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        mMainContainer.setSystemUiVisibility(flags);
    }

    private void setUpActiveView() {
        mActiveContainer = findViewById(R.id.active_container);
        mActiveButton = (Button) findViewById(R.id.activate_style_button);
        mActiveButton.setOnClickListener(this);
    }

    private void setUpDetailView() {
        mDetailContainer = findViewById(R.id.detail_container);
    }

    private void updateUiIfNeed() {
        if (needUpdateUi) {
            updateUi();
            needUpdateUi = false;
        }
    }

    private View getContainerFromMode(int uiMode) {
        switch (uiMode) {
            case MODE_DETAIL:
                return mDetailContainer;
            case MODE_ACTIVATE:
            default:
                return mActiveContainer;
        }
    }

    private void updateUi() {
        // default activate mode
        int newMode = MODE_ACTIVATE;
        if (mStyleActive) {
            newMode = MODE_DETAIL;
        }
        if (mUiMode == newMode) {
            return;
        }
        final View oldModeView = getContainerFromMode(mUiMode);
        View newModeView = getContainerFromMode(newMode);

        oldModeView.animate()
                .alpha(0)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        oldModeView.setVisibility(View.GONE);
                    }
                });

        if (newModeView.getAlpha() == 1) {
            newModeView.setAlpha(0);
        }
        newModeView.setVisibility(View.VISIBLE);
        newModeView.animate()
                .alpha(1)
                .setDuration(1000)
                .withEndAction(null);

        if (newMode == MODE_ACTIVATE) {


            final AnimatedStyleLogoFragment logoFragment = (AnimatedStyleLogoFragment)
                    getFragmentManager().findFragmentById(R.id.animated_logo_fragment);
            logoFragment.reset();
            logoFragment.setOnFillStartedCallback(new Runnable() {
                @Override
                public void run() {
                    mActiveButton.animate().alpha(1f).setDuration(500);
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logoFragment.start();
                }
            }, 1000);
        }

        if (mUiMode == MODE_ACTIVATE || newMode == MODE_ACTIVATE) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment demoFragment = fragmentManager.findFragmentById(R.id.demo_container_layout);
            if (newMode == MODE_ACTIVATE && demoFragment == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.demo_container_layout,
                                StyleRenderFragment.createInstance(true, true))
                        .commit();
            } else if (mUiMode == MODE_ACTIVATE && demoFragment != null) {
                fragmentManager.beginTransaction()
                        .remove(demoFragment)
                        .commit();
            }
        }

        if (newMode == MODE_DETAIL) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment detailFragment = fragmentManager.findFragmentById(R.id.detail_container);
            if (detailFragment == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.detail_container,
                                WallpaperDetailFragment.createInstance())
                        .commit();
            }
        }
        mUiMode = newMode;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setWallpaper() {
        try {
            startActivity(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            new ComponentName(StyleActivity.this, StyleWallpaperService.class))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(StyleActivity.this, "xxx", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activate_style_button:
                setWallpaper();
                break;
        }
    }

    @Override
    public void onStyleActivate() {
        mStyleActive = StyleConfig.isStyleActive();
        needUpdateUi = true;
    }

    private void initializeInjector() {
        wallpaperComponent = DaggerWallpaperComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
    }

    @Override
    public WallpaperComponent getComponent() {
        return wallpaperComponent;
    }

    @Override
    public void onSingleTapUp() {
        if (mUiMode == MODE_DETAIL) {
            showHideChrome((mMainContainer.getSystemUiVisibility()
                    & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0);
        }
    }
}
