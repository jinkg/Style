package com.yalin.style.view.activity

import android.animation.ObjectAnimator
import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.yalin.style.R
import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.view.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * @author jinyalin
 * *
 * @since 2017/5/2.
 */

class SettingsActivity : BaseActivity() {
    private var mBackgroundAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setContentView(R.layout.activity_settings)

        setupAppBar()

        drawInsetsFrameLayout.setOnInsetsCallback { insets ->
            val lp = mainContainer.layoutParams as ViewGroup.MarginLayoutParams
            lp.leftMargin = insets.left
            lp.topMargin = insets.top
            lp.rightMargin = insets.right
            lp.bottomMargin = insets.bottom
            mainContainer.layoutParams = lp
        }

        if (mBackgroundAnimator != null) {
            mBackgroundAnimator!!.cancel()
        }

        mBackgroundAnimator = ObjectAnimator.ofFloat(this, "backgroundOpacity", 0f, 1f)
        mBackgroundAnimator!!.duration = 1000
        mBackgroundAnimator!!.start()
    }

    private fun setupAppBar() {
        appBar.setNavigationOnClickListener { onNavigateUp() }

        inflateMenuFromFragment(0)
        appBar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reset -> {
                    val currentFragment = fragmentManager.findFragmentById(
                            R.id.contentContainer)
                    if (currentFragment != null
                            && currentFragment is SettingsActivityMenuListener) {
                        currentFragment
                                .onSettingsActivityMenuItemClick(item)
                    }
                    return@OnMenuItemClickListener true
                }
                R.id.action_about -> {
                    Analytics.logEvent(this, Event.ABOUT_OPEN)
                    startActivity(Intent(this, AboutActivity::class.java))
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        val newFragment = SettingsFragment.newInstance()
        fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .setTransitionStyle(R.style.Style_SimpleFadeFragmentAnimation)
                .replace(R.id.contentContainer, newFragment)
                .commit()
    }

    internal fun inflateMenuFromFragment(menuResId: Int) {
        appBar.menu.clear()
        if (menuResId != 0) {
            appBar.inflateMenu(menuResId)
        }
        appBar.inflateMenu(R.menu.menu_settings)
    }

    interface SettingsActivityMenuListener {
        fun onSettingsActivityMenuItemClick(item: MenuItem)
    }
}
