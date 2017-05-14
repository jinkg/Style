package com.yalin.style.view.component

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet

import com.yalin.style.R

/**
 * @author jinyalin
 * *
 * @since 2017/4/30.
 */

class TintableImageButton : android.support.v7.widget.AppCompatImageButton {

    private var tint: ColorStateList? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) :
            super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs,
                R.styleable.TintableImageButton, defStyle, 0)
        tint = a.getColorStateList(R.styleable.TintableImageButton_tint)
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (tint != null && tint!!.isStateful) {
            updateTintColor()
        }
    }

    fun setColorFilter(tint: ColorStateList) {
        this.tint = tint
        super.setColorFilter(tint.getColorForState(drawableState, 0))
    }

    private fun updateTintColor() {
        val color = tint!!.getColorForState(drawableState, 0)
        setColorFilter(color)
    }

}
