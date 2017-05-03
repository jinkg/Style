package com.yalin.style.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

import com.yalin.style.BuildConfig;
import com.yalin.style.R;
import com.yalin.style.view.fragment.AnimatedStyleLogoFragment;
import com.yalin.style.view.fragment.StyleRenderFragment;

/**
 * @author jinyalin
 * @since 2017/5/3.
 */

public class AboutActivity extends AppCompatActivity {

    private ViewPropertyAnimator mAnimator = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        ((Toolbar) findViewById(R.id.app_bar)).setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onNavigateUp();
                    }
                });

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.demo_view_container,
                            StyleRenderFragment.createInstance(true, false))
                    .commit();
        }

        // Build the about body view and append the link to see OSS licenses
        TextView versionView = (TextView) findViewById(R.id.app_version);
        versionView.setText(Html.fromHtml(
                getString(R.string.about_version_template, BuildConfig.VERSION_NAME)));

        TextView aboutBodyView = (TextView) findViewById(R.id.about_body);
        aboutBodyView.setText(Html.fromHtml(getString(R.string.about_body)));
        aboutBodyView.setMovementMethod(new LinkMovementMethod());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View demoContainerView = findViewById(R.id.demo_view_container);
        demoContainerView.setAlpha(0);
        mAnimator = demoContainerView.animate()
                .alpha(1)
                .setStartDelay(250)
                .setDuration(1000)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        AnimatedStyleLogoFragment logoFragment = (AnimatedStyleLogoFragment)
                                getFragmentManager().findFragmentById(R.id.animated_logo_fragment);
                        if (logoFragment != null) {
                            logoFragment.start();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}

