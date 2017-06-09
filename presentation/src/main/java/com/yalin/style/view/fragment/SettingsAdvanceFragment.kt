package com.yalin.style.view.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar

import com.yalin.style.R
import com.yalin.style.render.StyleBlurRenderer
import com.yalin.style.settings.Prefs
import com.yalin.style.view.activity.SettingsActivity
import kotlinx.android.synthetic.main.layout_include_settings_content.*

/**
 * @author jinyalin
 * *
 * @since 2017/5/2.
 */

class SettingsAdvanceFragment : BaseFragment(), SettingsActivity.SettingsActivityMenuListener {


    companion object {
        private val mHandler = Handler()
        fun newInstance(): SettingsAdvanceFragment {
            return SettingsAdvanceFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.layout_style_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onAttach(activity: Activity) {
        (activity as SettingsActivity).inflateMenuFromFragment(R.menu.menu_settings_advanced)
        super.onAttach(activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun setupViews() {
        blurAmount.apply {
            progress = Prefs.getSharedPreferences(activity)
                    .getInt(Prefs.PREF_BLUR_AMOUNT, StyleBlurRenderer.DEFAULT_BLUR)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mHandler.removeCallbacks(mUpdateBlurRunnable)
                        mHandler.postDelayed(mUpdateBlurRunnable, 750)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        dimAmount.apply {
            progress = Prefs.getSharedPreferences(activity)
                    .getInt(Prefs.PREF_DIM_AMOUNT, StyleBlurRenderer.DEFAULT_MAX_DIM)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mHandler.removeCallbacks(mUpdateDimRunnable)
                        mHandler.postDelayed(mUpdateDimRunnable, 750)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        greyAmount.apply {
            progress = Prefs.getSharedPreferences(activity)
                    .getInt(Prefs.PREF_GREY_AMOUNT, StyleBlurRenderer.DEFAULT_GREY)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mHandler.removeCallbacks(mUpdateGreyRunnable)
                        mHandler.postDelayed(mUpdateGreyRunnable, 750)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
        }

        blurOnLockscreen.apply {
            setOnCheckedChangeListener { _, checked ->
                Prefs.getSharedPreferences(activity).edit()
                        .putBoolean(Prefs.PREF_DISABLE_BLUR_WHEN_LOCKED, !checked)
                        .apply()
            }
            isChecked = !Prefs.getSharedPreferences(activity)
                    .getBoolean(Prefs.PREF_DISABLE_BLUR_WHEN_LOCKED, false)
        }
    }

    private val mUpdateBlurRunnable = Runnable {
        Prefs.getSharedPreferences(activity).edit()
                .putInt(Prefs.PREF_BLUR_AMOUNT, blurAmount.progress)
                .apply()
    }

    private val mUpdateDimRunnable = Runnable {
        Prefs.getSharedPreferences(activity).edit()
                .putInt(Prefs.PREF_DIM_AMOUNT, dimAmount.progress)
                .apply()
    }

    private val mUpdateGreyRunnable = Runnable {
        Prefs.getSharedPreferences(activity).edit()
                .putInt(Prefs.PREF_GREY_AMOUNT, greyAmount.progress)
                .apply()
    }

    override fun onSettingsActivityMenuItemClick(item: MenuItem) {
        if (item.itemId == R.id.action_reset) {
            Prefs.getSharedPreferences(activity).edit()
                    .putInt(Prefs.PREF_BLUR_AMOUNT, StyleBlurRenderer.DEFAULT_BLUR)
                    .putInt(Prefs.PREF_DIM_AMOUNT, StyleBlurRenderer.DEFAULT_MAX_DIM)
                    .putInt(Prefs.PREF_GREY_AMOUNT, StyleBlurRenderer.DEFAULT_GREY)
                    .apply()
            blurAmount.progress = StyleBlurRenderer.DEFAULT_BLUR
            dimAmount.progress = StyleBlurRenderer.DEFAULT_MAX_DIM
            greyAmount.progress = StyleBlurRenderer.DEFAULT_GREY
        }
    }

}
