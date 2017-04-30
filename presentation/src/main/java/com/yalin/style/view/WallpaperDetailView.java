package com.yalin.style.view;

import android.content.Intent;

import com.yalin.style.model.WallpaperItem;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */

public interface WallpaperDetailView extends LoadingDataView {

  void renderWallpaper(WallpaperItem wallpaperItem, boolean canKeep);

  void showNextButton(boolean show);

  void shareWallpaper(Intent shareIntent);

  void updateKeepWallpaper(boolean keeped);
}
