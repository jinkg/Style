package com.yalin.style.view.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.yalin.style.R
import java.util.*

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
class GalleryEmptyStateGraphicView(context: Context,
                                   attrs: AttributeSet) : View(context, attrs) {
    private val BITMAP = intArrayOf(
            0, 0, 1, 1, 1, 1, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 0, 0, 1, 1, 1,
            1, 1, 0, 1, 1, 0, 1, 1,
            1, 1, 0, 1, 1, 0, 1, 1,
            1, 1, 1, 0, 0, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1)

    private val COLS = 8
    private val ROWS = BITMAP.size / COLS

    private val CELL_SPACING_DIP = 2
    private val CELL_ROUNDING_DIP = 1
    private val CELL_SIZE_DIP = 8

    private val ON_TIME_MILLIS = 400
    private val FADE_TIME_MILLIS = 100
    private val OFF_TIME_MILLIS = 50

    private val mOffPaint = Paint()
    private val mOnPaint = Paint()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mOnTime: Long = 0
    private var mOnX: Int = 0
    private var mOnY: Int = 0
    private val mRandom = Random()
    private val mTempRectF = RectF()
    private var mCellSpacing: Int
    private var mCellRounding: Int
    private var mCellSize: Int

    init {
        val res = resources
        mOffPaint.isAntiAlias = true
        mOffPaint.color = ContextCompat.getColor(context, R.color.gallery_empty_state_dark)
        mOnPaint.isAntiAlias = true
        mOnPaint.color = ContextCompat.getColor(context, R.color.gallery_empty_state_light)

        mCellSpacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CELL_SPACING_DIP.toFloat(), res.displayMetrics).toInt()
        mCellSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CELL_SIZE_DIP.toFloat(), res.displayMetrics).toInt()
        mCellRounding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CELL_ROUNDING_DIP.toFloat(), res.displayMetrics).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (isShown) {
            postInvalidateOnAnimation()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
                View.resolveSize(COLS * mCellSize + (COLS - 1) * mCellSpacing, widthMeasureSpec),
                View.resolveSize(ROWS * mCellSize + (ROWS - 1) * mCellSpacing, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isShown || mWidth == 0 || mHeight == 0) {
            return
        }

        // tick timer
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed > mOnTime + ON_TIME_MILLIS.toLong() +
                (FADE_TIME_MILLIS * 2).toLong() + OFF_TIME_MILLIS.toLong()) {
            mOnTime = nowElapsed
            while (true) {
                val x = mRandom.nextInt(COLS)
                val y = mRandom.nextInt(ROWS)
                if ((x != mOnX || y != mOnY) && BITMAP[y * COLS + x] == 1) {
                    mOnX = x
                    mOnY = y
                    break
                }
            }
        }

        val t = (nowElapsed - mOnTime).toInt()
        for (y in 0..ROWS - 1) {
            for (x in 0..COLS - 1) {
                if (BITMAP[y * COLS + x] != 1) {
                    continue
                }

                mTempRectF.set(
                        (x * (mCellSize + mCellSpacing)).toFloat(),
                        (y * (mCellSize + mCellSpacing)).toFloat(),
                        (x * (mCellSize + mCellSpacing) + mCellSize).toFloat(),
                        (y * (mCellSize + mCellSpacing) + mCellSize).toFloat())

                canvas.drawRoundRect(mTempRectF,
                        mCellRounding.toFloat(),
                        mCellRounding.toFloat(),
                        mOffPaint)

                if (nowElapsed <= mOnTime + ON_TIME_MILLIS.toLong()
                        + (FADE_TIME_MILLIS * 2).toLong()
                        && mOnX == x && mOnY == y) {
                    // draw items
                    if (t < FADE_TIME_MILLIS) {
                        mOnPaint.alpha = t * 255 / FADE_TIME_MILLIS
                    } else if (t < FADE_TIME_MILLIS + ON_TIME_MILLIS) {
                        mOnPaint.alpha = 255
                    } else {
                        mOnPaint.alpha = 255 -
                                (t - ON_TIME_MILLIS - FADE_TIME_MILLIS) * 255 / FADE_TIME_MILLIS
                    }

                    canvas.drawRoundRect(mTempRectF,
                            mCellRounding.toFloat(),
                            mCellRounding.toFloat(),
                            mOnPaint)
                }
            }
        }

        postInvalidateOnAnimation()
    }
}