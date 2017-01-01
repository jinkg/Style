package com.yalin.style.provider;

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
  }

  public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

  private static final String PATH_WALLPAPER = "wallpaper";

  public static final class Wallpaper implements WallpaperColumns, BaseColumns {

    public static final String TABLE_NAME = "wallpaper";

    public static final String PATH_LAST_WALLPAPER = "last";

    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_WALLPAPER).build();

    public static final Uri CONTENT_LAST_WALLPAPER_URI =
        CONTENT_URI.buildUpon().appendPath(PATH_LAST_WALLPAPER).build();


    public static Uri buildWallpaperUri(String wallpaperId) {
      return CONTENT_URI.buildUpon().appendPath(wallpaperId).build();
    }

    public static String getWallpaperId(Uri uri) {
      return uri.getPathSegments().get(1);
    }
  }
}
