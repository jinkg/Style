package com.yalin.style.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yalin.style.R;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public class WallpaperDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_wallpaper_detail, container, false);
        return rootView;
    }
}
