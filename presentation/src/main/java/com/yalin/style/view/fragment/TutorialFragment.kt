package com.yalin.style.view.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView

import com.yalin.style.R
import com.yalin.style.event.MainContainerInsetsChangedEvent
import com.yalin.style.event.SeenTutorialEvent
import kotlinx.android.synthetic.main.layout_include_tutorial_content.*

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * @author jinyalin
 * *
 * @since 2017/5/2.
 */

class TutorialFragment : BaseFragment() {

    companion object {
        fun newInstance(): TutorialFragment {
            return TutorialFragment()
        }
    }

    private var animatorSet: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_include_tutorial_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tutorialIconAffordance.setOnClickListener {
            EventBus.getDefault().post(SeenTutorialEvent())
        }

        if (savedInstanceState == null) {
            val animateDistance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f,
                    resources.displayMetrics)
            tutorialMainText.alpha = 0f
            tutorialMainText.translationY = -animateDistance / 5

            tutorialSubText.alpha = 0f
            tutorialSubText.translationY = -animateDistance / 5

            tutorialIconAffordance.alpha = 0f
            tutorialIconAffordance.translationY = animateDistance

            tutorialIconText.alpha = 0f
            tutorialIconText.translationY = animateDistance

            AnimatorSet().apply {
                startDelay = 500
                duration = 250
                playTogether(
                        ObjectAnimator.ofFloat(tutorialMainText, View.ALPHA, 1f),
                        ObjectAnimator.ofFloat(tutorialSubText, View.ALPHA, 1f))
                start()
            }

            animatorSet = AnimatorSet().apply {
                startDelay = 2000
                // Bug in older versions where set.setInterpolator didn't work
                val interpolator = OvershootInterpolator()
                val a1 = ObjectAnimator.ofFloat<View>(tutorialIconAffordance, View.TRANSLATION_Y, 0f)
                val a2 = ObjectAnimator.ofFloat<View>(tutorialIconText, View.TRANSLATION_Y, 0f)
                val a3 = ObjectAnimator.ofFloat<View>(tutorialMainText, View.TRANSLATION_Y, 0f)
                val a4 = ObjectAnimator.ofFloat<View>(tutorialSubText, View.TRANSLATION_Y, 0f)
                a1.interpolator = interpolator
                a2.interpolator = interpolator
                a3.interpolator = interpolator
                a4.interpolator = interpolator
                duration = 500
                playTogether(
                        ObjectAnimator.ofFloat(tutorialIconAffordance, View.ALPHA, 1f),
                        ObjectAnimator.ofFloat(tutorialIconText, View.ALPHA, 1f),
                        a1, a2, a3, a4)
                if (Build.VERSION.SDK_INT >= 21) {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            playImageAvd(tutorialIconEmanate)
                        }
                    })
                }
                start()
            }
        } else {
            playImageAvd(tutorialIconEmanate)
        }

        val mcisce = EventBus.getDefault().getStickyEvent(
                MainContainerInsetsChangedEvent::class.java)
        if (mcisce != null) {
            onEventMainThread(mcisce)
        }
    }

    private fun playImageAvd(emanateView: ImageView) {
        if (Build.VERSION.SDK_INT >= 21) {
            val avd = resources.getDrawable(
                    R.drawable.avd_tutorial_icon_emanate,
                    activity.theme) as AnimatedVectorDrawable
            emanateView.setImageDrawable(avd)
            avd.start()
        }
    }

    override fun onDetach() {
        super.onDetach()
        animatorSet?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onEventMainThread(spe: MainContainerInsetsChangedEvent) {
        val insets = spe.insets
        tutorialContainer.setPadding(
                insets.left, insets.top, insets.right, insets.bottom)
    }

}
