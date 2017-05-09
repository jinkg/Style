package com.yalin.style.view.activity

import android.support.v7.app.AppCompatActivity

import com.yalin.style.StyleApplication
import com.yalin.style.injection.component.ApplicationComponent

/**
 * @author jinyalin
 * *
 * @since 2017/4/20.
 */

abstract class BaseActivity : AppCompatActivity() {
    protected val applicationComponent: ApplicationComponent
        get() = (application as StyleApplication).applicationComponent
}
