package com.yalin.style.data.entity;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    public boolean liked;
    public boolean isDefault;

    public String checksum;

    public WallpaperEntity() {
    }

    public static List<WallpaperEntity> readCursor(Context context, Cursor cursor) {
        List<WallpaperEntity> validWallpapers = new ArrayList<>(5);
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
        wallpaperEntity.liked = cursor.getInt(cursor.getColumnIndex(
                Wallpaper.COLUMN_NAME_LIKED)) == 1;
        wallpaperEntity.checksum = cursor.getString(cursor.getColumnIndex(
                Wallpaper.COLUMN_NAME_CHECKSUM));

        return wallpaperEntity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WallpaperEntity) {
            if (TextUtils.equals(((WallpaperEntity) obj).title, title)
                    && TextUtils.equals(((WallpaperEntity) obj).checksum, checksum)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + title.hashCode();
        result = 31 * result + checksum.hashCode();
        return result;
    }
}
