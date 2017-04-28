package com.yalin.style.view;

import com.yalin.style.model.WallpaperItem;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public interface WallpaperDetailView extends LoadingDataView {
    void renderWallpaper(WallpaperItem wallpaperItem);

    void showNextButton(boolean show);
}
