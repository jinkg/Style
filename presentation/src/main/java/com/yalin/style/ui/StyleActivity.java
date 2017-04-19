package com.yalin.style.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yalin.style.R;
import com.yalin.style.StyleWallpaperService;
import com.yalin.style.util.StyleConfig;

public class StyleActivity extends AppCompatActivity implements OnClickListener {

    private RelativeLayout mMainContainer;
    private View mActiveContainer;
    private Button mActiveButton;

    private Handler mHandler = new Handler();

    private boolean mStyleActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mMainContainer = (RelativeLayout) findViewById(R.id.main_container);
        showHideChrome(true);

        setUpActiveView();

        mStyleActive = StyleConfig.isStyleActive();

        updateUi();
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

    private void updateUi() {
        mActiveContainer.animate()
                .alpha(1)
                .setDuration(1000)
                .start();

        if (!mStyleActive) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment demoFragment = fragmentManager.findFragmentById(R.id.demo_container_layout);
            if (demoFragment == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.demo_container_layout,
                                StyleRenderFragment.createInstance(true, true))
                        .commit();
            }
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
}
