package com.yalin.style.view.fragment

import android.animation.ObjectAnimator
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
import com.yalin.style.domain.Source
import com.yalin.style.domain.interactor.DefaultObserver
import com.yalin.style.domain.interactor.GetSources
import com.yalin.style.injection.component.SourceComponent
import com.yalin.style.mapper.WallpaperItemMapper
import com.yalin.style.model.SourceItem
import com.yalin.style.view.component.ObservableHorizontalScrollView
import kotlinx.android.synthetic.main.layout_settings_choose_source.*
import java.util.*
import javax.inject.Inject

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class SettingsChooseSourceFragment : BaseFragment() {
    companion object {
        private val SCROLLBAR_HIDE_DELAY_MILLIS = 1000
        private val ALPHA_UNSELECTED = 0.4f
    }

    private val mHandler = Handler()
    private var mCurrentScroller: ObjectAnimator? = null

    private val mSources = ArrayList<SourceItem>()

    private val mHideScrollbarRunnable = { sourceScrollbar.hide() }

    private var mAnimationDuration: Int = 0
    private var mItemWidth: Int = 0
    private var mItemImageSize: Int = 0
    private var mItemEstimatedHeight: Int = 0

    private val mTempRectF = RectF()
    private val mImageFillPaint = Paint()
    private val mAlphaPaint = Paint()
    private var mSelectedSourceImage: Drawable? = null
    private var mSelectedSourceIndex: Int = 0

    @Inject
    lateinit internal var getSourcesUseCase: GetSources

    @Inject
    lateinit internal var wallpaperMapper: WallpaperItemMapper

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.layout_settings_choose_source, container, false)!!

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sourceScroller.setCallbacks(object : ObservableHorizontalScrollView.Callbacks {
            override fun onScrollChanged(scrollX: Int) {
                showScrollbar()
            }

            override fun onDownMotionEvent() {
                mCurrentScroller?.cancel()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        updateSources()
    }

    fun updateSources() {
        getSourcesUseCase.execute(object : DefaultObserver<List<Source>>() {
            override fun onNext(sources: List<Source>) {
                super.onNext(sources)
                mSources.addAll(wallpaperMapper.transformSources(sources))
                redrawSources()
            }
        }, null)
    }

    private fun redrawSources() {
        if (!isAdded) {
            return
        }

        sourceContainer.removeAllViews()
        for (source in mSources) {
            val rootView = LayoutInflater.from(activity).inflate(
                    R.layout.settings_choose_source_item, sourceContainer, false)

            rootView.alpha = ALPHA_UNSELECTED

            val selectSourceButton = rootView.findViewById(R.id.source_image)
            selectSourceButton.setOnClickListener {
                if (source.selected) {
                    (activity as Callbacks).onRequestCloseActivity()
                } else {

                }
            }
            val icon = BitmapDrawable(resources,
                    generateSourceImage(ContextCompat.getDrawable(activity, source.iconId)))
            icon.setColorFilter(source.color, PorterDuff.Mode.SRC_ATOP)
            selectSourceButton.background = icon

            val titleView = rootView.findViewById(R.id.source_title) as TextView
            titleView.text = source.title
            titleView.setTextColor(source.color)

            (rootView.findViewById(R.id.source_status) as TextView).text =
                    source.description

            val settingsButton = rootView.findViewById(R.id.source_settings_button)
//            CheatSheet.setup(source.settingsButton)
            settingsButton.setOnClickListener {
                //                launchSourceSettings(source)
            }

//            animateSettingsButton(settingsButton, false, false)

            sourceContainer.addView(rootView)
        }

        updateSelectedItem(false)
    }

    private fun updateSelectedItem(allowAnimate: Boolean) {
//        val previousSelectedSource = mSelectedSource
//        mSelectedSource = SourceManager.getSelectedSource(context)
//        if (previousSelectedSource != null && previousSelectedSource == mSelectedSource) {
//            // Only update status
//            for (source in mSources) {
//                if (source.componentName != mSelectedSource || source.rootView == null) {
//                    continue
//                }
//                updateSourceStatusUi(source)
//            }
//            return
//        }
//
//        // This is a newly selected source.
//        var selected: Boolean
//        var index = -1
//        for (source in mSources) {
//            ++index
//            if (source.componentName == previousSelectedSource) {
//                selected = false
//            } else if (source.componentName == mSelectedSource) {
//                mSelectedSourceIndex = index
//                selected = true
//            } else {
//                continue
//            }
//
//            if (source.rootView == null) {
//                continue
//            }
//
//            val sourceImageButton = source.rootView.findViewById(R.id.source_image)
//            val drawable = if (selected) mSelectedSourceImage else source.icon
//            drawable.setColorFilter(source.color, PorterDuff.Mode.SRC_ATOP)
//            sourceImageButton.setBackground(drawable)
//
//            val alpha = if (selected) 1f else ALPHA_UNSELECTED
//            source.rootView.animate()
//                    .alpha(alpha)
//                    .setDuration(mAnimationDuration.toLong())
//
//            if (selected) {
//                updateSourceStatusUi(source)
//            }
//
//            animateSettingsButton(source.settingsButton,
//                    selected && source.settingsActivity != null, allowAnimate)
//        }
//
//        if (mSelectedSourceIndex >= 0 && allowAnimate) {
//            if (mCurrentScroller != null) {
//                mCurrentScroller.cancel()
//            }
//
//            // For some reason smoothScrollTo isn't very smooth..
//            mCurrentScroller = ObjectAnimator.ofInt(mSourceScrollerView, "scrollX",
//                    mItemWidth * mSelectedSourceIndex)
//            mCurrentScroller.setDuration(mAnimationDuration.toLong())
//            mCurrentScroller.start()
//        }
    }

    private fun showScrollbar() {
        mHandler.removeCallbacks(mHideScrollbarRunnable)
        sourceScrollbar.setScrollRangeAndViewportWidth(
                sourceScroller.computeHorizontalScrollRange(),
                sourceScroller.width)
        sourceScrollbar.setScrollPosition(sourceScroller.scrollX)
        sourceScrollbar.show()
        mHandler.postDelayed(mHideScrollbarRunnable, SCROLLBAR_HIDE_DELAY_MILLIS.toLong())
    }

    private fun generateSourceImage(image: Drawable?): Bitmap {
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
        return bitmap
    }

    interface Callbacks {
        fun onRequestCloseActivity()
    }
}