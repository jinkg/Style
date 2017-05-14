package com.yalin.style.view.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu

import com.yalin.style.WallpaperDetailViewport
import com.yalin.style.R
import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.data.log.LogUtil
import com.yalin.style.event.MainContainerInsetsChangedEvent
import com.yalin.style.event.StyleWallpaperSizeChangedEvent
import com.yalin.style.event.SwitchingPhotosStateChangedEvent
import com.yalin.style.event.SystemWallpaperSizeChangedEvent
import com.yalin.style.injection.component.WallpaperComponent
import com.yalin.style.model.WallpaperItem
import com.yalin.style.presenter.WallpaperDetailPresenter
import com.yalin.style.util.ScrimUtil
import com.yalin.style.util.TypefaceUtil
import com.yalin.style.view.WallpaperDetailView
import com.yalin.style.view.activity.SettingsActivity
import com.yalin.style.view.component.PanScaleProxyView
import kotlinx.android.synthetic.main.layout_wallpaper_detail.*

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast

import javax.inject.Inject

/**
 * @author jinyalin
 * *
 * @since 2017/4/20.
 */

class WallpaperDetailFragment : BaseFragment(),
        WallpaperDetailView, View.OnSystemUiVisibilityChangeListener {

    companion object {
        val TAG = "WallpaperDetailFragment"
        fun createInstance(): WallpaperDetailFragment {
            return WallpaperDetailFragment()
        }
    }

    @Inject
    lateinit internal var presenter: WallpaperDetailPresenter

    private val overflowMenu: PopupMenu by lazy { PopupMenu(activity, btnOverflow) }

    private var currentViewportId = 0
    private var systemWallpaperAspectRatio: Float = 0.toFloat()
    private var styleWallpaperAspectRatio: Float = 0.toFloat()
    private var deferResetViewport: Boolean = false

    private var mGuardViewportChangeListener = false
    var isOverflowMenuVisible = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getComponent(WallpaperComponent::class.java).inject(this)

        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_wallpaper_detail, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDetailViews()
        presenter.setView(this)

        val syswsce = EventBus.getDefault().getStickyEvent(
                SystemWallpaperSizeChangedEvent::class.java)
        if (syswsce != null) {
            onEventMainThread(syswsce)
        }

        val swsce = EventBus.getDefault().getStickyEvent(
                StyleWallpaperSizeChangedEvent::class.java)
        if (swsce != null) {
            onEventMainThread(swsce)
        }

        val wdvp = EventBus.getDefault().getStickyEvent(
                WallpaperDetailViewport::class.java)
        if (wdvp != null) {
            onEventMainThread(wdvp)
        }

        val mcisce = EventBus.getDefault().getStickyEvent(
                MainContainerInsetsChangedEvent::class.java)
        if (mcisce != null) {
            onEventMainThread(mcisce)
        }

        val spsce = EventBus.getDefault().getStickyEvent(
                SwitchingPhotosStateChangedEvent::class.java)
        if (spsce != null) {
            onEventMainThread(spsce)
        }

        if (savedInstanceState == null) {
            loadWallpaper()
        } else {
            presenter.restoreInstanceState(savedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onStop() {
        super.onStop()
        overflowMenu.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        presenter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }


    private fun setupDetailViews() {
        metadata.setOnClickListener {
            val uri = "http://www.kinglloy.com"
            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Analytics.logEvent(context(), Event.VIEW_WALLPAPER_DETAIL)
            try {
                startActivity(viewIntent)
            } catch (e: RuntimeException) {
                toast(R.string.error_view_details)
                LogUtil.E(TAG, "Error viewing wallpaper details.", e)
                Analytics.logEvent(context(), Event.VIEW_WALLPAPER_DETAIL_FAILED)
            }
        }
        chromeContainer.background = ScrimUtil.makeCubicGradientScrimDrawable(
                0xaa000000.toInt(), 8, Gravity.BOTTOM)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            statusBarScrimView.visibility = View.GONE
        } else {
            statusBarScrimView.background = ScrimUtil.makeCubicGradientScrimDrawable(
                    0x44000000, 8, Gravity.TOP)
        }

        panScaleProxyView.setMaxZoom(5)
        panScaleProxyView.setOnViewportChangedListener(
                PanScaleProxyView.OnViewportChangedListener {
                    if (mGuardViewportChangeListener) {
                        return@OnViewportChangedListener
                    }
                    WallpaperDetailViewport.instance.setViewport(
                            currentViewportId, panScaleProxyView.currentViewport, true)
                })
        if (activity is PanScaleProxyView.OnOtherGestureListener) {
            panScaleProxyView.setOnOtherGestureListener(
                    activity as PanScaleProxyView.OnOtherGestureListener)
        }

        btnNext.setOnClickListener {
            Analytics.logEvent(activity, Event.SWITCH)
            presenter.getNextWallpaper()
        }
        setupOverflowButton()
    }

    private fun setupOverflowButton() {
        btnOverflow.setOnTouchListener(overflowMenu.dragToOpenListener)
        btnOverflow.setOnClickListener {
            isOverflowMenuVisible = true
            overflowMenu.show()
        }
        overflowMenu.setOnDismissListener { isOverflowMenuVisible = false }
        overflowMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_like -> {
                    presenter.likeWallpaper()
                    return@OnMenuItemClickListener true
                }
                R.id.action_share -> {
                    Analytics.logEvent(activity, Event.SHARE)
                    presenter.shareWallpaper()
                    return@OnMenuItemClickListener true
                }
                R.id.action_settings -> {
                    Analytics.logEvent(activity, Event.SETTINGS_OPEN)
                    startActivity(Intent(activity, SettingsActivity::class.java))
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        overflowMenu.menu.clear()
        overflowMenu.inflate(R.menu.style_overflow)
        overflowMenu.menu.add(0, R.id.action_share, 0, R.string.action_share)
    }

    private fun loadWallpaper() {
        presenter.initialize()
    }

    @Subscribe
    fun onEventMainThread(syswsce: SystemWallpaperSizeChangedEvent) {
        if (syswsce.height > 0) {
            systemWallpaperAspectRatio = syswsce.width * 1f / syswsce.height
        } else {
            systemWallpaperAspectRatio = panScaleProxyView.width * 1f / panScaleProxyView.height
        }
        resetProxyViewport()
    }

    @Subscribe
    fun onEventMainThread(swsce: StyleWallpaperSizeChangedEvent) {
        styleWallpaperAspectRatio = swsce.width * 1f / swsce.height
        resetProxyViewport()
    }

    @Subscribe
    fun onEventMainThread(e: WallpaperDetailViewport) {
        if (!e.isFromUser && panScaleProxyView != null) {
            mGuardViewportChangeListener = true
            panScaleProxyView.setViewport(e.getViewport(currentViewportId))
            mGuardViewportChangeListener = false
        }
    }

    @Subscribe
    fun onEventMainThread(spe: SwitchingPhotosStateChangedEvent) {
        currentViewportId = spe.currentId
        if (panScaleProxyView != null) {
            panScaleProxyView.enablePanScale(!spe.isSwitchingPhotos)
        }
        // Process deferred wallpaper size change when done switching
        if (!spe.isSwitchingPhotos && deferResetViewport) {
            resetProxyViewport()
        }
    }

    @Subscribe
    fun onEventMainThread(spe: MainContainerInsetsChangedEvent) {
        val insets = spe.insets
        chromeContainer.setPadding(
                insets.left, insets.top, insets.right, insets.bottom)
    }

    private fun resetProxyViewport() {
        if (systemWallpaperAspectRatio == 0f || styleWallpaperAspectRatio == 0f) {
            return
        }

        deferResetViewport = false
        val spe = EventBus.getDefault()
                .getStickyEvent(SwitchingPhotosStateChangedEvent::class.java)
        if (spe != null && spe.isSwitchingPhotos) {
            deferResetViewport = true
            return
        }

        panScaleProxyView.setRelativeAspectRatio(
                styleWallpaperAspectRatio / systemWallpaperAspectRatio)
    }

    override fun renderWallpaper(wallpaperItem: WallpaperItem) {
        val titleFont = "AlegreyaSans-Black.ttf"
        val bylineFont = "AlegreyaSans-Medium.ttf"
        tvTitle.typeface = TypefaceUtil.getAndCache(context(), titleFont)
        tvTitle.text = wallpaperItem.title
        tvAttribution.text = wallpaperItem.attribution
        tvByline.typeface = TypefaceUtil.getAndCache(context(), bylineFont)
        tvByline.text = wallpaperItem.byline

        Analytics.logEvent(activity, Event.PRESENT_WALLPAPER, wallpaperItem.title)
    }

    override fun showNextButton(show: Boolean) {
        btnNext.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun shareWallpaper(shareIntent: Intent) {
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(shareIntent)
    }

    override fun validLikeAction(valid: Boolean) {
        if (valid) {
            val keepItem = overflowMenu.menu.findItem(R.id.action_like)
            if (keepItem == null) {
                overflowMenu.menu.add(0, R.id.action_like, Menu.FIRST,
                        R.string.action_like)
            }
        } else {
            overflowMenu.menu.removeItem(R.id.action_like)
            btnNext.isActivated = false
        }
    }

    override fun updateLikeState(item: WallpaperItem, liked: Boolean) {
        overflowMenu.menu
                .findItem(R.id.action_like)
                .setTitle(if (liked) R.string.action_unlike else R.string.action_like)
        btnNext.isActivated = liked
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun showRetry() {

    }

    override fun hideRetry() {

    }

    override fun showError(message: String) {
        showToastMessage(message)
    }

    override fun context(): Context {
        return activity
    }

    override fun onSystemUiVisibilityChange(visibility: Int) {
        val visible = visibility and View.SYSTEM_UI_FLAG_LOW_PROFILE == 0

        val metadataSlideDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
        chromeContainer.visibility = View.VISIBLE
        chromeContainer.animate()
                .alpha(if (visible) 1f else 0f)
                .translationY(if (visible) 0f else metadataSlideDistance)
                .setDuration(200)
                .withEndAction {
                    if (!visible) {
                        chromeContainer.visibility = View.GONE
                    }
                }

        statusBarScrimView.visibility = View.VISIBLE
        statusBarScrimView.animate()
                .alpha(if (visible) 1f else 0f)
                .setDuration(200)
                .withEndAction {
                    if (!visible) {
                        statusBarScrimView.visibility = View.GONE
                    }
                }
    }

}
