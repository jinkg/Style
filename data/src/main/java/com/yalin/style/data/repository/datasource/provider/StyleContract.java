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
        String COLUMN_NAME_LIKED = "keep";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_CHECKSUM = "checksum";
    }

    interface GalleryColumns {

        /**
         * Type: TEXT
         */
        String COLUMN_NAME_CUSTOM_URI = "custom_wallpaper_uri";

        /**
         * Type: INTEGER
         */
        String COLUMN_NAME_IS_TREE_URI = "is_tree_uri";

        /**
         * Type: INTEGER
         */
        String COLUMN_NAME_DATE_TIME = "date_time";

        /**
         * Type: TEXT
         */
        String COLUMN_NAME_LOCATION = "location";

        /**
         * Type: INTEGER
         */
        String COLUMN_NAME_HAS_METADATA = "has_metadata";
    }

    interface AdvanceWallpaperColumns {
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_WALLPAPER_ID = "wallpaper_id";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_ICON_URL = "icon_url";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_NAME = "name";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_LINK = "link";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_AUTHOR = "author";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_DOWNLOAD_URL = "download_url";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_CHECKSUM = "checksum";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_STORE_PATH = "store_path";
        /**
         * Type: TEXT
         */
        String COLUMN_NAME_PROVIDER_NAME = "provider_name";
    }

    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    private static final String PATH_SOURCE = "source";
    private static final String PATH_WALLPAPER = "wallpaper";
    private static final String PATH_GALLERY = "gallery";
    private static final String PATH_ADVANCE_WALLPAPER = "advance_wallpaper";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_WALLPAPER
    };

    public static final class Source {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SOURCE).build();
    }

    public static final class Wallpaper implements WallpaperColumns, BaseColumns {

        public static final String TABLE_NAME = "wallpaper";

        public static final String PATH_LIKE_WALLPAPER = "like";
        public static final String PATH_LIKED_WALLPAPER = "liked";
        public static final String PATH_SAVE_WALLPAPER = "save";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WALLPAPER).build();

        public static final Uri CONTENT_KEEPED_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WALLPAPER)
                        .appendPath(PATH_LIKED_WALLPAPER).build();

        public static Uri buildWallpaperUri(String wallpaperId) {
            return CONTENT_URI.buildUpon().appendPath(wallpaperId).build();
        }

        public static Uri buildWallpaperSaveUri(String wallpaperId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_SAVE_WALLPAPER).appendPath(wallpaperId).build();
        }

        public static Uri buildWallpaperLikeUri(String wallpaperId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_LIKE_WALLPAPER).appendPath(wallpaperId).build();
        }


        public static String getWallpaperId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getWallpaperSaveId(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getWallpaperLikeId(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class GalleryWallpaper implements GalleryColumns, BaseColumns {

        public static final String TABLE_NAME = "gallery_wallpaper";

        public static final String PATH_URI = "uri";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GALLERY).build();

        public static Uri buildGalleryWallpaperUri(long insertId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(insertId)).build();
        }

        public static String getGalleryWallpaperId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildGalleryWallpaperDeleteUri(String uri) {
            return CONTENT_URI.buildUpon().appendPath(PATH_URI)
                    .appendPath(String.valueOf(uri)).build();
        }

        public static String getGalleryWallpaperDeleteUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class AdvanceWallpaper implements AdvanceWallpaperColumns, BaseColumns {
        public static final String TABLE_NAME = "advance_wallpaper";

        public static final String PATH_SAVE_WALLPAPER = "save";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_ADVANCE_WALLPAPER).build();

        public static Uri buildWallpaperUri(String wallpaperId) {
            return CONTENT_URI.buildUpon().appendPath(wallpaperId).build();
        }

        public static String getWallpaperId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
