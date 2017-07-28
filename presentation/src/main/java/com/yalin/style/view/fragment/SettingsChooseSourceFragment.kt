package com.yalin.style.view.fragment

import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yalin.style.R
import com.yalin.style.analytics.Analytics
import com.yalin.style.analytics.Event
import com.yalin.style.data.log.LogUtil
import com.yalin.style.domain.repository.SourcesRepository
import com.yalin.style.injection.component.SourceComponent
import com.yalin.style.model.SourceItem
import com.yalin.style.presenter.SettingsChooseSourcePresenter
import com.yalin.style.view.SourceChooseView
import com.yalin.style.view.activity.AdvanceSettingActivity
import com.yalin.style.view.activity.GallerySettingActivity
import com.yalin.style.view.component.ObservableHorizontalScrollView
import kotlinx.android.synthetic.main.layout_settings_choose_source.*
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class SettingsChooseSourceFragment : BaseFragment(), SourceChooseView {

    companion object {
        private val TAG = "SettingsChooseSourceFragment"
        private val SCROLLBAR_HIDE_DELAY_MILLIS = 1000
        private val ALPHA_UNSELECTED = 0.4f
    }

    private val mHandler = Handler()
    private var mCurrentScroller: ObjectAnimator? = null

    private val mHideScrollbarRunnable = {
        if (sourceScrollbar != null) {
            sourceScrollbar.hide()
        }
    }

    private var mAnimationDuration: Int = 0
    private var mItemWidth: Int = 0
    private var mItemImageSize: Int = 0
    private var mItemEstimatedHeight: Int = 0

    private val mTempRectF = RectF()
    private val mImageFillPaint = Paint()
    private val mAlphaPaint = Paint()
    private var mSelectedSourceImage: Drawable? = null

    @Inject
    lateinit internal var settingsPresenter: SettingsChooseSourcePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        mItemWidth = resources.getDimensionPixelSize(
                R.dimen.settings_choose_source_item_width)
        mItemEstimatedHeight = resources.getDimensionPixelSize(
                R.dimen.settings_choose_source_item_estimated_height)
        mItemImageSize = resources.getDimensionPixelSize(
                R.dimen.settings_choose_source_item_image_size)

        getComponent(SourceComponent::class.java).inject(this)

        prepareGenerateSourceImages()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.layout_settings_choose_source, container, false)!!

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsPresenter.setView(this)

        sourceScroller.setCallbacks(object : ObservableHorizontalScrollView.Callbacks {
            override fun onScrollChanged(scrollX: Int) {
                showScrollbar()
            }

            override fun onDownMotionEvent() {
                mCurrentScroller?.cancel()
            }

        })

        settingsPresenter.initialize()
    }

    override fun onResume() {
        super.onResume()
        settingsPresenter.resume()
    }

    override fun onPause() {
        super.onPause()
        settingsPresenter.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        settingsPresenter.destroy()
    }

    override fun renderSources(sources: List<SourceItem>) {
        redrawSources(sources)
    }

    override fun sourceSelected(sources: List<SourceItem>, selectedItem: SourceItem) {
        updateSelectedItem(sources, selectedItem, true)
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
    }

    override fun context(): Context {
        return activity
    }

    val sourcesMap = HashMap<Int, View>()
    private fun redrawSources(sources: List<SourceItem>) {
        if (!isAdded) {
            return
        }
        sourcesMap.clear()
        sourceContainer.removeAllViews()
        for (source in sources) {
            val rootView = LayoutInflater.from(activity).inflate(
                    R.layout.settings_choose_source_item, sourceContainer, false)
            sourcesMap.put(source.id, rootView)

            with(rootView!!) {
                alpha = ALPHA_UNSELECTED

                val selectSourceButton = findViewById(R.id.source_image)
                selectSourceButton.setOnClickListener {
                    if (source.selected) {
                        (activity as Callbacks).onRequestCloseActivity()
                    } else {
                        Analytics.logEvent(context, Event.SELECT_WALLPAPER_SOURCE, source.title!!)
                        settingsPresenter.selectSource(source.id)
                    }
                }
                val icon = generateSourceImage(source.iconId)
                icon.setColorFilter(source.color, PorterDuff.Mode.SRC_ATOP)
                selectSourceButton.background = icon

                adjustSourceColor(source)

                val titleView = findViewById(R.id.source_title) as TextView
                titleView.text = source.title
                titleView.setTextColor(source.color)

                (findViewById(R.id.source_status) as TextView).text =
                        source.description

                val settingsButton = findViewById(R.id.source_settings_button)
                settingsButton.setOnClickListener {
                    Analytics.logEvent(context, Event.CUSTOM_WALLPAPER_SETTINGS)
                    launchSourceSettings(source)
                }

                animateSettingsButton(settingsButton, false, false)

                sourceContainer.addView(rootView)
            }
        }
    }

    private fun updateSelectedItem(sources: List<SourceItem>,
                                   selectedItem: SourceItem, allowAnimate: Boolean) {
        var selectedIndex = -1
        var index = -1
        for (source in sources) {
            index++
            val selected = source.id == selectedItem.id
            selectedIndex = if (selected) index else selectedIndex

            with(sourcesMap[source.id]!!) {
                val sourceImageButton = findViewById(R.id.source_image)
                val drawable = if (selected)
                    mSelectedSourceImage else
                    generateSourceImage(source.iconId)
                drawable!!.setColorFilter(source.color, PorterDuff.Mode.SRC_ATOP)
                sourceImageButton.background = drawable

                val alpha = if (source.selected) 1f else ALPHA_UNSELECTED
                animate().alpha(alpha).duration = mAnimationDuration.toLong()

                val settingsButton = findViewById(R.id.source_settings_button)
                animateSettingsButton(settingsButton,
                        source.selected && source.hasSetting, allowAnimate)
            }
        }

        if (selectedIndex >= 0 && allowAnimate) {
            mCurrentScroller?.cancel()

            // For some reason smoothScrollTo isn't very smooth..
            mCurrentScroller = ObjectAnimator.ofInt(sourceScroller, "scrollX",
                    mItemWidth * selectedIndex)
            mCurrentScroller!!.duration = mAnimationDuration.toLong()
            mCurrentScroller!!.start()
        }
    }

    private fun animateSettingsButton(settingsButton: View, show: Boolean,
                                      allowAnimate: Boolean) {
        if (show && settingsButton.visibility == View.VISIBLE ||
                !show && settingsButton.visibility == View.INVISIBLE) {
            return
        }
        settingsButton.visibility = View.VISIBLE
        settingsButton.animate()
                .translationY((if (show)
                    0
                else
                    -resources.getDimensionPixelSize(
                            R.dimen.settings_choose_source_settings_button_animate_distance))
                        .toFloat())
                .alpha(if (show) 1f else 0f)
                .rotation((if (show) 0 else -90).toFloat())
                .setDuration((if (allowAnimate) 300 else 0).toLong())
                .setStartDelay((if (show && allowAnimate) 200 else 0).toLong())
                .withLayer()
                .withEndAction {
                    if (!show) {
                        settingsButton.visibility = View.INVISIBLE
                    }
                }
    }

    private fun showScrollbar() {
        mHandler.removeCallbacks(mHideScrollbarRunnable)
        sourceScrollbar?.setScrollRangeAndViewportWidth(
                sourceScroller.computeHorizontalScrollRange(),
                sourceScroller.width)
        sourceScrollbar?.setScrollPosition(sourceScroller.scrollX)
        sourceScrollbar?.show()
        mHandler.postDelayed(mHideScrollbarRunnable, SCROLLBAR_HIDE_DELAY_MILLIS.toLong())
    }

    private fun generateSourceImage(iconId: Int): BitmapDrawable {
        val image = ContextCompat.getDrawable(activity, iconId)

        val bitmap = Bitmap.createBitmap(mItemImageSize, mItemImageSize,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        mTempRectF.set(0f, 0f, mItemImageSize.toFloat(), mItemImageSize.toFloat())
        canvas.drawOval(mTempRectF, mImageFillPaint)
        if (image != null) {
            canvas.saveLayer(0f, 0f, mItemImageSize.toFloat(), mItemImageSize.toFloat(),
                    mAlphaPaint, Canvas.ALL_SAVE_FLAG)
            image.setBounds(0, 0, mItemImageSize, mItemImageSize)
            image.draw(canvas)
            canvas.restore()
        }
        return BitmapDrawable(resources, bitmap)
    }

    private fun prepareGenerateSourceImages() {
        mImageFillPaint.color = Color.WHITE
        mImageFillPaint.isAntiAlias = true
        mAlphaPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        mSelectedSourceImage = generateSourceImage(R.drawable.ic_source_selected)
    }

    private fun adjustSourceColor(source: SourceItem) = with(source) {
        try {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            var adjust = false
            if (hsv[2] < 0.8f) {
                hsv[2] = 0.8f
                adjust = true
            }
            if (hsv[1] > 0.4f) {
                hsv[1] = 0.4f
                adjust = true
            }
            if (adjust) {
                color = Color.HSVToColor(hsv)
            }
            if (Color.alpha(color) != 255) {
                color = Color.argb(255,
                        Color.red(color),
                        Color.green(color),
                        Color.blue(color))
            }
        } catch (ignored: IllegalArgumentException) {
        }
    }

    private fun launchSourceSettings(source: SourceItem) {
        try {
            if (source.id == SourcesRepository.SOURCE_ID_CUSTOM) {
                val settingsIntent = Intent(activity, GallerySettingActivity::class.java)
                startActivity(settingsIntent)
            } else if (source.id == SourcesRepository.SOURCE_ID_ADVANCE) {
                val settingsIntent = Intent(activity, AdvanceSettingActivity::class.java)
                startActivity(settingsIntent)
            }
        } catch (e: ActivityNotFoundException) {
            LogUtil.E(TAG, "Can't launch source settings.", e)
        } catch (e: SecurityException) {
            LogUtil.E(TAG, "Can't launch source settings.", e)
        }

    }

    interface Callbacks {
        fun onRequestCloseActivity()
    }
}