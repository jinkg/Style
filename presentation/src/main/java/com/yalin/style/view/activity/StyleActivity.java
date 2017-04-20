package com.yalin.style.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yalin.style.R;
import com.yalin.style.StyleWallpaperService;
import com.yalin.style.injection.HasComponent;
import com.yalin.style.injection.component.DaggerWallpaperComponent;
import com.yalin.style.injection.component.WallpaperComponent;
import com.yalin.style.view.fragment.AnimatedStyleLogoFragment;
import com.yalin.style.view.fragment.StyleRenderFragment;
import com.yalin.style.util.StyleConfig;

public class StyleActivity extends BaseActivity implements OnClickListener,
        StyleConfig.ActivateListener, HasComponent<WallpaperComponent> {

    private RelativeLayout mMainContainer;
    private View mActiveContainer;
    private View mDetailContainer;
    private Button mActiveButton;

    private Handler mHandler = new Handler();

    private boolean mStyleActive = false;

    private WallpaperComponent wallpaperComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeInjector();

        setContentView(R.layout.activity_main);

        mMainContainer = (RelativeLayout) findViewById(R.id.main_container);
        showHideChrome(true);

        setUpActiveView();
        setUpDetailView();

        mStyleActive = StyleConfig.isStyleActive();

        StyleConfig.registerActivateListener(this);

        updateUi();
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

    private void updateUi() {
        mActiveContainer.animate()
                .alpha(1)
                .setDuration(1000)
                .start();

        if (mStyleActive) {
            mActiveContainer.setVisibility(View.GONE);
            mDetailContainer.setVisibility(View.VISIBLE);
        } else {
            mActiveContainer.setVisibility(View.VISIBLE);
            mDetailContainer.setVisibility(View.GONE);
        }

        if (!mStyleActive) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment demoFragment = fragmentManager.findFragmentById(R.id.demo_container_layout);
            if (demoFragment == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.demo_container_layout,
                                StyleRenderFragment.createInstance(true, true))
                        .commit();
            }

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
        }else{

        }
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
        updateUi();
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
}
