package com.yalin.style.view.activity;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yalin.style.R;
import com.yalin.style.analytics.Analytics;
import com.yalin.style.analytics.Event;
import com.yalin.style.view.component.DrawInsetsFrameLayout;
import com.yalin.style.view.fragment.SettingsFragment;

/**
 * @author jinyalin
 * @since 2017/5/2.
 */

public class SettingsActivity extends BaseActivity {
    Toolbar mAppBar;

    private ObjectAnimator mBackgroundAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(R.layout.activity_settings);

        setupAppBar();

        ((DrawInsetsFrameLayout) findViewById(R.id.draw_insets_frame_layout)).setOnInsetsCallback(
                new DrawInsetsFrameLayout.OnInsetsCallback() {
                    @Override
                    public void onInsetsChanged(Rect insets) {
                        View container = findViewById(R.id.container);
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                                container.getLayoutParams();
                        lp.leftMargin = insets.left;
                        lp.topMargin = insets.top;
                        lp.rightMargin = insets.right;
                        lp.bottomMargin = insets.bottom;
                        container.setLayoutParams(lp);
                    }
                });

        if (mBackgroundAnimator != null) {
            mBackgroundAnimator.cancel();
        }

        mBackgroundAnimator = ObjectAnimator.ofFloat(this, "backgroundOpacity", 0, 1);
        mBackgroundAnimator.setDuration(1000);
        mBackgroundAnimator.start();
    }

    private void setupAppBar() {
        mAppBar = (Toolbar) findViewById(R.id.app_bar);
        mAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigateUp();
            }
        });

        inflateMenuFromFragment(0);
        mAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_reset:
                        Fragment currentFragment = getFragmentManager().findFragmentById(
                                R.id.content_container);
                        if (currentFragment != null
                                && currentFragment instanceof SettingsActivityMenuListener) {
                            ((SettingsActivityMenuListener) currentFragment)
                                    .onSettingsActivityMenuItemClick(item);
                        }
                        return true;
                    case R.id.action_about:
                        Analytics.logEvent(SettingsActivity.this, Event.ABOUT_OPEN);
                        startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                        return true;
                }
                return false;
            }
        });
        Fragment newFragment = SettingsFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .setTransitionStyle(R.style.Style_SimpleFadeFragmentAnimation)
                .replace(R.id.content_container, newFragment)
                .commit();
    }

    void inflateMenuFromFragment(int menuResId) {
        if (mAppBar == null) {
            return;
        }

        mAppBar.getMenu().clear();
        if (menuResId != 0) {
            mAppBar.inflateMenu(menuResId);
        }
        mAppBar.inflateMenu(R.menu.menu_settings);
    }

    public interface SettingsActivityMenuListener {
        void onSettingsActivityMenuItemClick(MenuItem item);
    }
}
