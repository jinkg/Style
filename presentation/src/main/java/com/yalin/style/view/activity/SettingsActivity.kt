package com.yalin.style.view.activity

import android.animation.ObjectAnimator
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView

import com.yalin.style.R
import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.injection.HasComponent
import com.yalin.style.injection.component.DaggerSourceComponent
import com.yalin.style.injection.component.SourceComponent
import com.yalin.style.view.fragment.SettingsAdvanceFragment
import com.yalin.style.view.fragment.SettingsChooseSourceFragment
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * @author jinyalin
 * *
 * @since 2017/5/2.
 */

class SettingsActivity : BaseActivity(), HasComponent<SourceComponent>,
        SettingsChooseSourceFragment.Callbacks {

    companion object {
        val START_SECTION_SOURCE = 0
        val START_SECTION_ADVANCED = 1

        val SECTION_LABELS =
                intArrayOf(R.string.section_choose_source, R.string.section_advanced)

        private val SECTION_FRAGMENTS =
                arrayOf<Class<*>>(SettingsChooseSourceFragment::class.java,
                        SettingsAdvanceFragment::class.java)
    }

    private val sourceComponent: SourceComponent by lazy { initializeInjector() }

    private var mBackgroundAnimator: ObjectAnimator? = null
    private var mStartSection = START_SECTION_SOURCE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setContentView(R.layout.activity_settings)

        getSectionFromIntent(intent)
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

    private fun getSectionFromIntent(intent: Intent) {
        val uri = intent.data
        uri?.let {
            val path = uri.pathSegments[0]
            when (path) {
                "advance" -> {
                    mStartSection = START_SECTION_ADVANCED
                    Analytics.logEvent(this, Event.SHORTCUTS_ADVANCE_SETTINGS)
                }
                else -> {
                    mStartSection = START_SECTION_SOURCE
                    Analytics.logEvent(this, Event.SHORTCUTS_SETTINGS)
                }
            }
        }
    }

    private fun initializeInjector() = DaggerSourceComponent.builder()
            .applicationComponent(applicationComponent)
            .build()

    private fun setupAppBar() {
        appBar.setNavigationOnClickListener { onNavigateUp() }

        val inflater = LayoutInflater.from(this)
        sectionSpinner.adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return SECTION_LABELS.size
            }

            override fun getItem(position: Int): Any {
                return SECTION_LABELS[position]
            }

            override fun getItemId(position: Int): Long {
                return (position + 1).toLong()
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var view = convertView
                if (view == null) {
                    view = inflater.inflate(R.layout.settings_ab_spinner_list_item,
                            parent, false)
                }
                (view!!.findViewById(android.R.id.text1) as TextView).text =
                        getString(SECTION_LABELS[position])
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup):
                    View {
                var view = convertView
                if (view == null) {
                    view = inflater.inflate(R.layout.settings_ab_spinner_list_item_dropdown,
                            parent, false)
                }
                (view!!.findViewById(android.R.id.text1) as TextView).text =
                        getString(SECTION_LABELS[position])
                return view as View
            }
        }

        sectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(spinner: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                val fragmentClass = SECTION_FRAGMENTS[position]
                val currentFragment = fragmentManager.findFragmentById(
                        R.id.contentContainer)
                if (currentFragment != null && fragmentClass == currentFragment.javaClass) {
                    return
                }

                inflateMenuFromFragment(0)

                try {
                    val newFragment = fragmentClass.newInstance()
                    fragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .setTransitionStyle(R.style.Style_SimpleFadeFragmentAnimation)
                            .replace(R.id.contentContainer, newFragment as Fragment)
                            .commitAllowingStateLoss()
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }

            }

            override fun onNothingSelected(spinner: AdapterView<*>) {}
        }

        sectionSpinner.setSelection(mStartSection)

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

            val currentFragment = supportFragmentManager.findFragmentById(
                    R.id.contentContainer)
            if (currentFragment != null && currentFragment is SettingsActivityMenuListener) {
                currentFragment.onSettingsActivityMenuItemClick(item)
            }

            false
        })
    }

    fun inflateMenuFromFragment(menuResId: Int) {
        appBar.menu.clear()
        if (menuResId != 0) {
            appBar.inflateMenu(menuResId)
        }
        appBar.inflateMenu(R.menu.menu_settings)
    }

    override val component: SourceComponent
        get() = sourceComponent

    override fun onRequestCloseActivity() {
        finish()
    }

    interface SettingsActivityMenuListener {
        fun onSettingsActivityMenuItemClick(item: MenuItem)
    }
}
