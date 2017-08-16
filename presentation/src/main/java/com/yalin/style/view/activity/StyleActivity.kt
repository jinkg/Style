package com.yalin.style.view.activity

import android.Manifest
import android.app.Fragment
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.yalin.style.R
import com.yalin.style.StyleApplication
import com.yalin.style.StyleWallpaperService
import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.data.BuildConfig
import com.yalin.style.data.log.LogUtil
import com.yalin.style.data.repository.AdvanceWallpaperDataRepository
import com.yalin.style.event.MainContainerInsetsChangedEvent
import com.yalin.style.event.SeenTutorialEvent
import com.yalin.style.event.WallpaperActivateEvent
import com.yalin.style.event.WallpaperDetailOpenedEvent
import com.yalin.style.injection.HasComponent
import com.yalin.style.injection.component.DaggerWallpaperComponent
import com.yalin.style.injection.component.WallpaperComponent
import com.yalin.style.view.component.PanScaleProxyView
import com.yalin.style.view.fragment.AnimatedStyleLogoFragment
import com.yalin.style.view.fragment.StyleRenderFragment
import com.yalin.style.view.fragment.TutorialFragment
import com.yalin.style.view.fragment.WallpaperDetailFragment

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_include_wallpaper_detail.*
import kotlinx.android.synthetic.main.layout_include_wallpaper_tutorial.*
import kotlinx.android.synthetic.main.layout_include_active.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/5/9.
 */
class StyleActivity : BaseActivity(), HasComponent<WallpaperComponent>,
        PanScaleProxyView.OnOtherGestureListener {

    companion object {
        private val TAG = "StyleActivity"
        // ui mode
        private val MODE_UNKNOWN = -1
        private val MODE_ACTIVATE = 0
        private val MODE_DETAIL = 1
        private val MODE_TUTORIAL = 2

        private val PREF_SEEN_TUTORIAL = "seen_tutorial"

        private val REQUEST_PERMISSION_CODE = 10000

        private val mHandler = Handler()

    }

    private val wallpaperComponent: WallpaperComponent by lazy { initializeInjector() }

    private var mUiMode = MODE_UNKNOWN

    private var mWindowHasFocus = false
    private var mPaused = false

    private var mStyleActive = false

    private var mSeenTutorial = false

    private var wallpaperDetailFragment: WallpaperDetailFragment? = null

    private var startLogAnim: Runnable? = null

    @Inject
    lateinit var advanceWallpaperRepository: AdvanceWallpaperDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StyleApplication.instance.applicationComponent.inject(this)
        advanceWallpaperRepository.maybeRollback()

        Analytics.setUserProperty(this, "device_type", "Android")
        Analytics.onStartSession(this)

        setupActiveView()
        setupDetailView()
        setupTutorialView()

        mainContainer.setOnInsetsCallback {
            insets ->
            EventBus.getDefault().postSticky(MainContainerInsetsChangedEvent(insets))
        }

        showHideChrome(true)

        EventBus.getDefault().register(this)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        mSeenTutorial = sp.getBoolean(PREF_SEEN_TUTORIAL, false)
    }

    override fun onStart() {
        super.onStart()
        if (BuildConfig.ENABLE_EXTERNAL_LOG &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_CODE)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        mPaused = false

        // update intro mode UI to latest wallpaper active state
        val e = EventBus.getDefault()
                .getStickyEvent(WallpaperActivateEvent::class.java)
        if (e != null) {
            onEventMainThread(e)
        } else {
            onEventMainThread(WallpaperActivateEvent(false))
        }

        updateUi()

        val decorView = window.decorView
        decorView.alpha = 0f
        decorView.animate().cancel()
        decorView.animate()
                .setStartDelay(500)
                .alpha(1f).duration = 300

        maybeUpdateWallpaperDetailOpenedClosed()
    }

    override fun onPause() {
        super.onPause()
        mPaused = true
        maybeUpdateWallpaperDetailOpenedClosed()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        Analytics.onEndSession(this)

        if (startLogAnim != null) mHandler.removeCallbacks(startLogAnim)
    }

    private fun showHideChrome(show: Boolean) {
        var flags = if (show) 0 else View.SYSTEM_UI_FLAG_LOW_PROFILE
        flags = flags or
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        if (!show) {
            flags = flags or
                    (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
        mainContainer.systemUiVisibility = flags
    }

    private fun setupActiveView() {
        activateStyleButton.setOnClickListener {
            Analytics.logEvent(this, Event.ACTIVATE)
            setWallpaper()
        }
    }

    private fun setupDetailView() {
    }

    private fun setupTutorialView() {
    }

    private fun getContainerFromMode(uiMode: Int): View {
        when (uiMode) {
            MODE_DETAIL -> return detailContainer
            MODE_TUTORIAL -> return tutorialContainer
            else -> return activeContainer
        }
    }

    private fun updateUi() {
        // default activate mode
        var newMode = MODE_ACTIVATE
        if (mStyleActive) {
            newMode = MODE_TUTORIAL
            if (mSeenTutorial) {
                newMode = MODE_DETAIL
            }
        }
        if (mUiMode == newMode) {
            return
        }

        LogUtil.D(TAG, "update UI")

        val oldModeView = getContainerFromMode(mUiMode)
        val newModeView = getContainerFromMode(newMode)

        oldModeView.animate()
                .alpha(0f)
                .setDuration(1000)
                .withEndAction { oldModeView.visibility = View.GONE }

        if (newModeView.alpha == 1f) {
            newModeView.alpha = 0f
        }
        newModeView.visibility = View.VISIBLE
        newModeView.animate()
                .alpha(1f)
                .setDuration(1000)
                .withEndAction(null)

        if (newMode == MODE_ACTIVATE) {
            val logoFragment = fragmentManager.findFragmentById(R.id.animatedLogoFragment)
                    as AnimatedStyleLogoFragment
            logoFragment.reset()
            logoFragment.setOnFillStartedCallback {
                activateStyleButton.animate().alpha(1f).duration = 500
            }
            startLogAnim = Runnable { logoFragment.start() }
            mHandler.postDelayed(startLogAnim, 1000)
        }

        if (mUiMode == MODE_ACTIVATE || newMode == MODE_ACTIVATE) {
            val demoFragment = fragmentManager.findFragmentById(R.id.demoContainerLayout)
            if (newMode == MODE_ACTIVATE && demoFragment == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.demoContainerLayout,
                                StyleRenderFragment.createInstance(true, true))
                        .commit()
            } else if (mUiMode == MODE_ACTIVATE && demoFragment != null) {
                fragmentManager.beginTransaction()
                        .remove(demoFragment)
                        .commit()
            }
        }

        if (newMode == MODE_DETAIL) {
            val detailFragment = fragmentManager.findFragmentById(R.id.detailContainer)
            if (detailFragment == null) {
                wallpaperDetailFragment = WallpaperDetailFragment.createInstance()
                fragmentManager.beginTransaction()
                        .add(R.id.detailContainer, wallpaperDetailFragment)
                        .commit()
            } else {
                wallpaperDetailFragment = detailFragment as WallpaperDetailFragment?
            }

            mainContainer.setOnSystemUiVisibilityChangeListener(wallpaperDetailFragment)
        }

        if (mUiMode == MODE_TUTORIAL || newMode == MODE_TUTORIAL) {
            var tutorialFragment: Fragment? = fragmentManager.findFragmentById(R.id.mainContainer)
            if (newMode == MODE_TUTORIAL && tutorialFragment == null) {
                Analytics.logEvent(this, Event.TUTORIAL_BEGIN)
                tutorialFragment = TutorialFragment.newInstance()
                fragmentManager.beginTransaction()
                        .add(R.id.mainContainer, tutorialFragment)
                        .commit()
            } else if (mUiMode == MODE_TUTORIAL && tutorialFragment != null) {
                Analytics.logEvent(this, Event.TUTORIAL_COMPLETE)
                fragmentManager.beginTransaction()
                        .remove(tutorialFragment)
                        .commit()
            }
        }

        mUiMode = newMode

        maybeUpdateWallpaperDetailOpenedClosed()
    }

    private fun maybeUpdateWallpaperDetailOpenedClosed() {
        var currentlyOpened = false
        val wdoe = EventBus.getDefault()
                .getStickyEvent(WallpaperDetailOpenedEvent::class.java)
        if (wdoe != null) {
            currentlyOpened = wdoe.isWallpaperDetailOpened
        }

        var shouldBeOpened = false

        if (mUiMode == MODE_DETAIL) {
            val overflowMenuVisible = wallpaperDetailFragment != null
                    && wallpaperDetailFragment!!.isOverflowMenuVisible
            if ((mWindowHasFocus || overflowMenuVisible) && !mPaused) {
                shouldBeOpened = true
            }
        }

        if (currentlyOpened != shouldBeOpened) {
            EventBus.getDefault().postSticky(WallpaperDetailOpenedEvent(shouldBeOpened))
        }
    }

    @Subscribe
    fun onEventMainThread(e: WallpaperActivateEvent) {
        if (mPaused) {
            return
        }

        mStyleActive = e.isWallpaperActivate
        updateUi()
    }

    @Subscribe
    fun onEventMainThread(e: SeenTutorialEvent) {
        mSeenTutorial = true
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(PREF_SEEN_TUTORIAL, true)
                .apply()
        updateUi()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mWindowHasFocus = hasFocus
        maybeUpdateWallpaperDetailOpenedClosed()
    }

    private fun initializeInjector() = DaggerWallpaperComponent.builder()
            .applicationComponent(applicationComponent)
            .build()


    private fun setWallpaper() {
        try {
            startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(this, StyleWallpaperService::class.java))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e2: ActivityNotFoundException) {
                toast(R.string.exception_message_device_unsupported)
                Analytics.logEvent(this, Event.DEVICE_UNSUPPORTED)
            }
        }
    }

    override val component: WallpaperComponent
        get() = wallpaperComponent

    override fun onSingleTapUp() {
        if (mUiMode == MODE_DETAIL) {
            showHideChrome(mainContainer.systemUiVisibility
                    and View.SYSTEM_UI_FLAG_LOW_PROFILE != 0)
        }
    }
}