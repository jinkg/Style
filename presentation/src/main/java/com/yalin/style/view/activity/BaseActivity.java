package com.yalin.style.view.activity;

import android.support.v7.app.AppCompatActivity;

import com.yalin.style.StyleApplication;
import com.yalin.style.injection.component.ApplicationComponent;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected ApplicationComponent getApplicationComponent() {
        return ((StyleApplication) getApplication()).getApplicationComponent();
    }
}
