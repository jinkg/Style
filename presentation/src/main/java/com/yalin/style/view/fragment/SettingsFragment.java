package com.yalin.style.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.yalin.style.R;
import com.yalin.style.render.StyleBlurRenderer;
import com.yalin.style.settings.Prefs;
import com.yalin.style.view.activity.SettingsActivity;

/**
 * @author jinyalin
 * @since 2017/5/2.
 */

public class SettingsFragment extends BaseFragment implements
        SettingsActivity.SettingsActivityMenuListener {

    private Handler mHandler = new Handler();
    private SeekBar mBlurSeekBar;
    private SeekBar mDimSeekBar;
    private SeekBar mGreySeekBar;


    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_style_settings, container, false);

        setupViews(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void setupViews(View rootView) {
        mBlurSeekBar = (SeekBar) rootView.findViewById(R.id.blur_amount);
        mBlurSeekBar.setProgress(Prefs.getSharedPreferences(getActivity())
                .getInt(Prefs.PREF_BLUR_AMOUNT, StyleBlurRenderer.DEFAULT_BLUR));
        mBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    mHandler.removeCallbacks(mUpdateBlurRunnable);
                    mHandler.postDelayed(mUpdateBlurRunnable, 750);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mDimSeekBar = (SeekBar) rootView.findViewById(R.id.dim_amount);
        mDimSeekBar.setProgress(Prefs.getSharedPreferences(getActivity())
                .getInt(Prefs.PREF_DIM_AMOUNT, StyleBlurRenderer.DEFAULT_MAX_DIM));
        mDimSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    mHandler.removeCallbacks(mUpdateDimRunnable);
                    mHandler.postDelayed(mUpdateDimRunnable, 750);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mGreySeekBar = (SeekBar) rootView.findViewById(R.id.grey_amount);
        mGreySeekBar.setProgress(Prefs.getSharedPreferences(getActivity())
                .getInt(Prefs.PREF_GREY_AMOUNT, StyleBlurRenderer.DEFAULT_GREY));
        mGreySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    mHandler.removeCallbacks(mUpdateGreyRunnable);
                    mHandler.postDelayed(mUpdateGreyRunnable, 750);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private Runnable mUpdateBlurRunnable = new Runnable() {
        @Override
        public void run() {
            Prefs.getSharedPreferences(getActivity()).edit()
                    .putInt(Prefs.PREF_BLUR_AMOUNT, mBlurSeekBar.getProgress())
                    .apply();
        }
    };

    private Runnable mUpdateDimRunnable = new Runnable() {
        @Override
        public void run() {
            Prefs.getSharedPreferences(getActivity()).edit()
                    .putInt(Prefs.PREF_DIM_AMOUNT, mDimSeekBar.getProgress())
                    .apply();
        }
    };

    private Runnable mUpdateGreyRunnable = new Runnable() {
        @Override
        public void run() {
            Prefs.getSharedPreferences(getActivity()).edit()
                    .putInt(Prefs.PREF_GREY_AMOUNT, mGreySeekBar.getProgress())
                    .apply();
        }
    };

    @Override
    public void onSettingsActivityMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_reset) {
            Prefs.getSharedPreferences(getActivity()).edit()
                    .putInt(Prefs.PREF_BLUR_AMOUNT, StyleBlurRenderer.DEFAULT_BLUR)
                    .putInt(Prefs.PREF_DIM_AMOUNT, StyleBlurRenderer.DEFAULT_MAX_DIM)
                    .putInt(Prefs.PREF_GREY_AMOUNT, StyleBlurRenderer.DEFAULT_GREY)
                    .apply();
            mBlurSeekBar.setProgress(StyleBlurRenderer.DEFAULT_BLUR);
            mDimSeekBar.setProgress(StyleBlurRenderer.DEFAULT_MAX_DIM);
            mGreySeekBar.setProgress(StyleBlurRenderer.DEFAULT_GREY);
        }
    }
}
