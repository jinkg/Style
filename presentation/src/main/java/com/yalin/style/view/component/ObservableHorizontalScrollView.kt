package com.yalin.style.view.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

/**
 * @author jinyalin
 * @since 2017/5/22.
 */
class ObservableHorizontalScrollView(context: Context, attrs: AttributeSet) :
        HorizontalScrollView(context, attrs) {
    private var mCallbacks: Callbacks? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        mCallbacks?.onScrollChanged(l)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> mCallbacks?.onDownMotionEvent()
        }
        return super.onTouchEvent(ev)
    }

    public override fun computeHorizontalScrollRange(): Int {
        return super.computeHorizontalScrollRange()
    }

    fun setCallbacks(listener: Callbacks) {
        mCallbacks = listener
    }

    interface Callbacks {
        fun onScrollChanged(scrollX: Int)
        fun onDownMotionEvent()
    }
}
