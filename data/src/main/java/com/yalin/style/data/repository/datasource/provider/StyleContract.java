package com.yalin.style.data.repository.datasource.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * YaLin 2016/12/30.
 */

public class StyleContract {

  public static final String AUTHORITY = "com.yalin.style";

  private static final String SCHEME = "content://";

  interface WallpaperColumns {

    /**
     * Type: TEXT
     */
    String COLUMN_NAME_WALLPAPER_ID = "wallpaper_id";
    /**
     * Type: TEXT
     */
    String COLUMN_NAME_IMAGE_URI = "image_uri";
    /**
     * Type: TEXT
     */
    String COLUMN_NAME_TITLE = "title";
    /**
     * Type: TEXT
     */
    String COLUMN_NAME_BYLINE = "byline";
    /**
     * Type: TEXT
     */
    String COLUMN_NAME_ATTRIBUTION = "attribution";
    /**
     * Type: long
     */
    String COLUMN_NAME_ADD_DATE = "add_date";
    /**
     * Type: SHORT
     */
    String COLUMN_NAME_KEEP = "keep";
  }

  public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

  private static final String PATH_WALLPAPER = "wallpaper";

  public static final String[] TOP_LEVEL_PATHS = {
      PATH_WALLPAPER
  };

  public static final class Wallpaper implements WallpaperColumns, BaseColumns {

    public static final String TABLE_NAME = "wallpaper";

    public static final String PATH_KEEP_WALLPAPER = "keep";
    public static final String PATH_KEEPED_WALLPAPER = "keeped";
    public static final String PATH_SAVE_WALLPAPER = "save";

    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_WALLPAPER).build();

    public static final Uri CONTENT_KEEPED_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_WALLPAPER)
            .appendPath(PATH_KEEPED_WALLPAPER).build();

    public static Uri buildWallpaperUri(String wallpaperId) {
      return CONTENT_URI.buildUpon().appendPath(wallpaperId).build();
    }

    public static Uri buildWallpaperSaveUri(String wallpaperId) {
      return CONTENT_URI.buildUpon()
          .appendPath(PATH_SAVE_WALLPAPER).appendPath(wallpaperId).build();
    }

    public static Uri buildWallpaperKeepUri(String wallpaperId) {
      return CONTENT_URI.buildUpon()
          .appendPath(PATH_KEEP_WALLPAPER).appendPath(wallpaperId).build();
    }


    public static String getWallpaperId(Uri uri) {
      return uri.getPathSegments().get(1);
    }

    public static String getWallpaperSaveId(Uri uri) {
      return uri.getPathSegments().get(2);
    }

    public static String getWallpaperKeepId(Uri uri) {
      return uri.getPathSegments().get(2);
    }
  }
}
