package com.yalin.style.view.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.yalin.style.R
import com.yalin.style.util.MathUtil

/**
 * @author jinyalin
 * @since 2017/5/22.
 */

class Scrollbar @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        View(context, attrs, defStyle) {


    companion object {
        private val DEFAULT_BACKGROUND_COLOR = 0x80000000.toInt()
        private val DEFAULT_INDICATOR_COLOR = 0xff000000.toInt()
    }

    private var mHidden = true

    private val mAnimationDuration: Int =
            resources.getInteger(android.R.integer.config_shortAnimTime)
    private var mIndicatorWidth: Float = 0.toFloat()
    private val mBackgroundPaint: Paint
    private val mIndicatorPaint: Paint

    private val mTempPath = Path()
    private val mTempRectF = RectF()

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var mScrollRange: Int = 0
    private var mViewportWidth: Int = 0
    private var mPosition: Float = 0.toFloat()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Scrollbar)
        val mBackgroundColor = a.getColor(R.styleable.Scrollbar_backgroundColor,
                DEFAULT_BACKGROUND_COLOR)
        val mIndicatorColor = a.getColor(R.styleable.Scrollbar_indicatorColor,
                DEFAULT_INDICATOR_COLOR)
        a.recycle()

        mBackgroundPaint = Paint()
        mBackgroundPaint.color = mBackgroundColor
        mBackgroundPaint.isAntiAlias = true

        mIndicatorPaint = Paint()
        mIndicatorPaint.color = mIndicatorColor
        mIndicatorPaint.isAntiAlias = true

        alpha = 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mScrollRange <= mViewportWidth) {
            return
        }

        mTempRectF.top = 0f
        mTempRectF.bottom = mHeight.toFloat()
        mTempRectF.left = 0f
        mTempRectF.right = mWidth.toFloat()

        drawPill(canvas, mTempRectF, mBackgroundPaint)

        mTempRectF.top = 0f
        mTempRectF.bottom = mHeight.toFloat()
        mTempRectF.left = mPosition * 1f / (mScrollRange - mViewportWidth) *
                mWidth.toFloat() * (1 - mIndicatorWidth)
        mTempRectF.right = mTempRectF.left + mIndicatorWidth * mWidth

        drawPill(canvas, mTempRectF, mIndicatorPaint)
    }

    private fun drawPill(canvas: Canvas, rectF: RectF, paint: Paint) {
        val radius = rectF.height() / 2
        var temp: Float

        mTempPath.reset()
        mTempPath.moveTo(rectF.left + radius, rectF.top)
        mTempPath.lineTo(rectF.right - radius, rectF.top)

        temp = rectF.left
        rectF.left = rectF.right - 2 * radius
        mTempPath.arcTo(rectF, 270f, 180f)
        rectF.left = temp

        mTempPath.lineTo(rectF.left + radius, rectF.bottom)

        temp = rectF.right
        rectF.right = rectF.left + rectF.height()
        mTempPath.arcTo(rectF, 90f, 180f)
        rectF.right = temp

        mTempPath.close()
        canvas.drawPath(mTempPath, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
                View.resolveSize(0, widthMeasureSpec),
                View.resolveSize(0, heightMeasureSpec))
    }

    fun setScrollPosition(position: Int) {
        mPosition = MathUtil.constrain(0f, mScrollRange.toFloat(), position.toFloat())
        postInvalidateOnAnimation()
    }

    fun setScrollRangeAndViewportWidth(scrollRange: Int, viewportWidth: Int) {
        mScrollRange = scrollRange
        mViewportWidth = viewportWidth
        mIndicatorWidth = 0.1f
        if (mScrollRange > 0) {
            mIndicatorWidth = MathUtil.constrain(mIndicatorWidth, 1f,
                    mViewportWidth * 1f / mScrollRange)
        }
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = h
        mWidth = w
    }

    fun show() {
        if (!mHidden) {
            return
        }

        mHidden = false
        animate().cancel()
        animate().alpha(1f).duration = mAnimationDuration.toLong()
    }

    fun hide() {
        if (mHidden) {
            return
        }

        mHidden = true
        animate().cancel()
        animate().alpha(0f).duration = mAnimationDuration.toLong()
    }

}
