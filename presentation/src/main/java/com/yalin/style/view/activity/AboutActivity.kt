package com.yalin.style.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewPropertyAnimator

import com.yalin.style.BuildConfig
import com.yalin.style.R
import com.yalin.style.view.fragment.AnimatedStyleLogoFragment
import com.yalin.style.view.fragment.StyleRenderFragment
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_include_about_content.*

/**
 * @author jinyalin
 * *
 * @since 2017/5/3.
 */

class AboutActivity : AppCompatActivity() {

    private var mAnimator: ViewPropertyAnimator? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        appBar.setNavigationOnClickListener { onNavigateUp() }

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.demoViewContainer,
                            StyleRenderFragment.createInstance(true, true))
                    .commit()
        }

        // Build the about body view and append the link to see OSS licenses
        appVersion.text = Html.fromHtml(
                getString(R.string.about_version_template, BuildConfig.VERSION_NAME))

        aboutBody.text = Html.fromHtml(getString(R.string.about_body))
        aboutBody.movementMethod = LinkMovementMethod()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        demoViewContainer.alpha = 0f
        mAnimator = demoViewContainer.animate()
                .alpha(1f)
                .setStartDelay(250)
                .setDuration(1000)
                .withEndAction {
                    val logoFragment = fragmentManager.findFragmentById(R.id.animatedLogoFragment)
                            as AnimatedStyleLogoFragment
                    logoFragment.start()
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAnimator != null) {
            mAnimator!!.cancel()
        }
    }
}

