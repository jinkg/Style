package com.yalin.style.view.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.yalin.style.R;
import com.yalin.style.StyleWallpaperService;
import com.yalin.style.analytics.Analytics;
import com.yalin.style.analytics.Event;
import com.yalin.style.data.BuildConfig;
import com.yalin.style.event.MainContainerInsetsChangedEvent;
import com.yalin.style.event.SeenTutorialEvent;
import com.yalin.style.event.WallpaperActivateEvent;
import com.yalin.style.event.WallpaperDetailOpenedEvent;
import com.yalin.style.injection.HasComponent;
import com.yalin.style.injection.component.DaggerWallpaperComponent;
import com.yalin.style.injection.component.WallpaperComponent;
import com.yalin.style.view.component.DrawInsetsFrameLayout;
import com.yalin.style.view.component.PanScaleProxyView;
import com.yalin.style.view.fragment.AnimatedStyleLogoFragment;
import com.yalin.style.view.fragment.StyleRenderFragment;
import com.yalin.style.view.fragment.TutorialFragment;
import com.yalin.style.view.fragment.WallpaperDetailFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class StyleActivity extends BaseActivity implements OnClickListener,
        HasComponent<WallpaperComponent>, PanScaleProxyView.OnOtherGestureListener {
    // ui mode
    private static final int MODE_UNKNOWN = -1;
    private static final int MODE_ACTIVATE = 0;
    private static final int MODE_DETAIL = 1;
    private static final int MODE_TUTORIAL = 2;

    private static final String PREF_SEEN_TUTORIAL = "seen_tutorial";

    private static final int REQUEST_PERMISSION_CODE = 10000;

    private DrawInsetsFrameLayout mMainContainer;
    private View mActiveContainer;
    private View mDetailContainer;
    private View mTutorialContainer;
    private Button mActiveButton;

    private Handler mHandler = new Handler();

    private WallpaperComponent wallpaperComponent;

    private int mUiMode = MODE_UNKNOWN;

    private boolean mWindowHasFocus;
    private boolean mPaused = false;

    private boolean mStyleActive = false;

    private boolean mSeenTutorial = false;

    WallpaperDetailFragment wallpaperDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeInjector();

        setContentView(R.layout.activity_main);

        Analytics.setUserProperty(this, "device_type", "Android");

        setupActiveView();
        setupDetailView();
        setupTutorialView();

        mMainContainer = (DrawInsetsFrameLayout) findViewById(R.id.main_container);
        mMainContainer.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                EventBus.getDefault().postSticky(new MainContainerInsetsChangedEvent(insets));
            }
        });

        showHideChrome(true);

        EventBus.getDefault().register(this);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mSeenTutorial = sp.getBoolean(PREF_SEEN_TUTORIAL, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (BuildConfig.ENABLE_EXTERNAL_LOG &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mPaused = false;

        // update intro mode UI to latest wallpaper active state
        WallpaperActivateEvent e = EventBus.getDefault()
                .getStickyEvent(WallpaperActivateEvent.class);
        if (e != null) {
            onEventMainThread(e);
        } else {
            onEventMainThread(new WallpaperActivateEvent(false));
        }

        updateUi();

        View decorView = getWindow().getDecorView();
        decorView.setAlpha(0f);
        decorView.animate().cancel();
        decorView.animate()
                .setStartDelay(500)
                .alpha(1f)
                .setDuration(300);

        maybeUpdateWallpaperDetailOpenedClosed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        maybeUpdateWallpaperDetailOpenedClosed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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

    private void setupActiveView() {
        mActiveContainer = findViewById(R.id.active_container);
        mActiveButton = (Button) findViewById(R.id.activate_style_button);
        mActiveButton.setOnClickListener(this);
    }

    private void setupDetailView() {
        mDetailContainer = findViewById(R.id.detail_container);
    }

    private void setupTutorialView() {
        mTutorialContainer = findViewById(R.id.tutorial_container);
    }

    private View getContainerFromMode(int uiMode) {
        switch (uiMode) {
            case MODE_DETAIL:
                return mDetailContainer;
            case MODE_TUTORIAL:
                return mTutorialContainer;
            case MODE_ACTIVATE:
            default:
                return mActiveContainer;
        }
    }

    private void updateUi() {
        // default activate mode
        int newMode = MODE_ACTIVATE;
        if (mStyleActive) {
            newMode = MODE_TUTORIAL;
            if (mSeenTutorial) {
                newMode = MODE_DETAIL;
            }
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
                wallpaperDetailFragment = WallpaperDetailFragment.createInstance();
                mMainContainer.setOnSystemUiVisibilityChangeListener(wallpaperDetailFragment);
                fragmentManager.beginTransaction()
                        .add(R.id.detail_container, wallpaperDetailFragment)
                        .commit();
            }
        }

        if (mUiMode == MODE_TUTORIAL || newMode == MODE_TUTORIAL) {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment tutorialFragment = fragmentManager.findFragmentById(R.id.main_container);
            if (newMode == MODE_TUTORIAL && tutorialFragment == null) {
                Analytics.logEvent(this, Event.TUTORIAL_BEGIN, null);
                tutorialFragment = TutorialFragment.newInstance();
                fragmentManager.beginTransaction()
                        .add(R.id.main_container, tutorialFragment)
                        .commit();
            } else if (mUiMode == MODE_TUTORIAL && tutorialFragment != null) {
                Analytics.logEvent(this, Event.TUTORIAL_COMPLETE, null);
                fragmentManager.beginTransaction()
                        .remove(tutorialFragment)
                        .commit();
            }
        }

        mUiMode = newMode;

        maybeUpdateWallpaperDetailOpenedClosed();
    }

    private void maybeUpdateWallpaperDetailOpenedClosed() {
        boolean currentlyOpened = false;
        WallpaperDetailOpenedEvent wdoe = EventBus.getDefault()
                .getStickyEvent(WallpaperDetailOpenedEvent.class);
        if (wdoe != null) {
            currentlyOpened = wdoe.isWallpaperDetailOpened();
        }

        boolean shouldBeOpened = false;

        if (mUiMode == MODE_DETAIL) {
            boolean overflowMenuVisible = wallpaperDetailFragment != null
                    && wallpaperDetailFragment.isOverflowMenuVisible();
            if ((mWindowHasFocus || overflowMenuVisible)
                    && !mPaused) {
                shouldBeOpened = true;
            }
        }

        if (currentlyOpened != shouldBeOpened) {
            EventBus.getDefault().postSticky(new WallpaperDetailOpenedEvent(shouldBeOpened));
        }
    }

    @Subscribe
    public void onEventMainThread(final WallpaperActivateEvent e) {
        if (mPaused) {
            return;
        }

        mStyleActive = e.isWallpaperActivate();
        updateUi();
    }

    @Subscribe
    public void onEventMainThread(final SeenTutorialEvent e) {
        mSeenTutorial = true;
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(PREF_SEEN_TUTORIAL, true)
                .apply();
        updateUi();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mWindowHasFocus = hasFocus;
        maybeUpdateWallpaperDetailOpenedClosed();
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
                Toast.makeText(StyleActivity.this,
                        R.string.exception_message_device_unsupported, Toast.LENGTH_LONG).show();
                Analytics.logEvent(this, Event.DEVICE_UNSUPPORTED, null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activate_style_button:
                Analytics.logEvent(this, Event.ACTIVATE, null);
                setWallpaper();
                break;
        }
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
