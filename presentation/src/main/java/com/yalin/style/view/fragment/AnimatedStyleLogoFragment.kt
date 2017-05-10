package com.yalin.style.view.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Fragment
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import com.yalin.style.R
import kotlinx.android.synthetic.main.animated_logo_fragment.*

/**
 * YaLin 2017/1/4.
 */

class AnimatedStyleLogoFragment : Fragment() {

    private var mOnFillStartedCallback: (() -> Unit)? = null
    private var mInitialLogoOffset: Float = 0.toFloat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mInitialLogoOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f,
                resources.displayMetrics)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.animated_logo_fragment, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reset()
    }

    fun start() {
        logoView.animate().translationY(0f).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                logoSubtitle.visibility = View.VISIBLE
                logoSubtitle.translationY = (-logoSubtitle.height).toFloat()
                val interpolator = OvershootInterpolator()
                logoSubtitle.animate()
                        .translationY(0f)
                        .setInterpolator(interpolator).setDuration(500)
                        .start()

                mOnFillStartedCallback?.invoke()

            }
        }).setDuration(500).start()

    }

    fun setOnFillStartedCallback(fillStartedCallback: () -> Unit) {
        mOnFillStartedCallback = fillStartedCallback
    }

    fun reset() {
        logoView.translationY = mInitialLogoOffset
        logoSubtitle.visibility = View.INVISIBLE
    }
}
