package com.yalin.style.data.entity;

import android.content.Context;
import android.database.Cursor;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class WallpaperEntity {

  private static final String TAG = "WallpaperEntity";

  public int id;
  public String wallpaperId;
  public String imageUri;
  public String title;
  public String byline;
  public String attribution;

  public boolean keep;
  public boolean isDefault;

  public String checksum;

  public WallpaperEntity() {
  }

  public static Set<WallpaperEntity> readCursor(Context context, Cursor cursor) {
    Set<WallpaperEntity> validWallpapers = new HashSet<>();
    while (cursor != null && cursor.moveToNext()) {
      WallpaperEntity wallpaperEntity = WallpaperEntity.readEntityFromCursor(cursor);
      try {
        // valid input stream
        InputStream is = context.getContentResolver().openInputStream(
            StyleContract.Wallpaper.buildWallpaperUri(
                wallpaperEntity.wallpaperId));
        validWallpapers.add(wallpaperEntity);
        if (is != null) {
          is.close();
        }
      } catch (Exception e) {
        LogUtil.D(TAG, "File not found with wallpaper id : "
            + wallpaperEntity.wallpaperId);
      }
    }
    return validWallpapers;
  }

  public static WallpaperEntity readEntityFromCursor(Cursor cursor) {
    WallpaperEntity wallpaperEntity = new WallpaperEntity();

    wallpaperEntity.id = cursor.getInt(cursor.getColumnIndex(
        Wallpaper._ID));
    wallpaperEntity.title = cursor.getString(cursor.getColumnIndex(
        Wallpaper.COLUMN_NAME_TITLE));
    wallpaperEntity.wallpaperId = cursor.getString(cursor.getColumnIndex(
        Wallpaper.COLUMN_NAME_WALLPAPER_ID));
    wallpaperEntity.imageUri = cursor.getString(cursor.getColumnIndex(
        Wallpaper.COLUMN_NAME_IMAGE_URI));
    wallpaperEntity.byline = cursor.getString(cursor.getColumnIndex(
        Wallpaper.COLUMN_NAME_BYLINE));
    wallpaperEntity.attribution = cursor.getString(cursor.getColumnIndex(
        Wallpaper.COLUMN_NAME_ATTRIBUTION));
    wallpaperEntity.keep = cursor.getInt(cursor.getColumnIndex(
        Wallpaper.COLUMN_NAME_KEEP)) == 1;

    return wallpaperEntity;
  }
}
