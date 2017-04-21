package com.yalin.style.data.repository.datasource;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.yalin.style.data.cache.WallpaperCache;
import com.yalin.style.data.entity.WallpaperEntity;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class DbWallpaperDataStore implements WallpaperDataStore {
    private static final String TAG = "DbWallpaperDataStore";

    private static final String DEFAULT_WALLPAPER_ID = "-1";

    private final Context context;

    private final WallpaperCache wallpaperCache;

    public DbWallpaperDataStore(Context context, WallpaperCache wallpaperCache) {
        this.context = context;
        this.wallpaperCache = wallpaperCache;

        getWallPaperEntity().doOnNext(wallpaperCache::put);
    }

    @Override
    public Observable<WallpaperEntity> getWallPaperEntity() {
        return createEntityObservable().doOnNext(wallpaperCache::put);
    }

    @Override
    public Observable<InputStream> openInputStream(String wallpaperId) {
        return Observable.create(emitter -> {
            try {
                InputStream inputStream;
                if (DEFAULT_WALLPAPER_ID.equals(wallpaperId)) {
                    inputStream = context.getAssets().open("painterly-architectonic.jpg");
                } else {
                    inputStream = context.getContentResolver().openInputStream(
                            StyleContract.Wallpaper.buildWallpaperUri(wallpaperId));
                }
                emitter.onNext(inputStream);
                emitter.onComplete();
            } catch (IOException e) {
                LogUtil.D(TAG, "Open input stream failed for id : " + wallpaperId);
                emitter.onError(e);
            }
        });
    }

    private Observable<WallpaperEntity> createEntityObservable() {
        return Observable.create(emitter -> {
            Cursor cursor = null;
            WallpaperEntity validWallpaper = null;
            try {
                ContentResolver contentResolver = context.getContentResolver();
                cursor = contentResolver.query(StyleContract.Wallpaper.CONTENT_URI,
                        null, null, null, null);
                while (cursor != null && cursor.moveToNext()) {
                    WallpaperEntity wallpaperEntity = readCursor(cursor);
                    try {
                        // valid input stream
                        contentResolver.openInputStream(
                                StyleContract.Wallpaper.buildWallpaperUri(
                                        wallpaperEntity.wallpaperId));
                        validWallpaper = wallpaperEntity;
                        break;
                    } catch (Exception e) {
                        LogUtil.D(TAG, "File not found with wallpaper id : "
                                + wallpaperEntity.wallpaperId);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (validWallpaper == null) {
                validWallpaper = buildDefaultWallpaper();
            }

            emitter.onNext(validWallpaper);
            emitter.onComplete();
        });
    }

    private WallpaperEntity readCursor(Cursor cursor) {
        WallpaperEntity wallpaperEntity = new WallpaperEntity();

        wallpaperEntity.title = cursor.getString(cursor.getColumnIndex(
                StyleContract.Wallpaper.COLUMN_NAME_TITLE));
        wallpaperEntity.wallpaperId = cursor.getString(cursor.getColumnIndex(
                StyleContract.Wallpaper.COLUMN_NAME_WALLPAPER_ID));
        wallpaperEntity.imageUri = cursor.getString(cursor.getColumnIndex(
                StyleContract.Wallpaper.COLUMN_NAME_IMAGE_URI));
        wallpaperEntity.byline = cursor.getString(cursor.getColumnIndex(
                StyleContract.Wallpaper.COLUMN_NAME_BYLINE));
        wallpaperEntity.attribution = cursor.getString(cursor.getColumnIndex(
                StyleContract.Wallpaper.COLUMN_NAME_ATTRIBUTION));

        return wallpaperEntity;
    }

    private WallpaperEntity buildDefaultWallpaper() {
        WallpaperEntity wallpaperEntity = new WallpaperEntity();
        wallpaperEntity.attribution = "kinglloy.com";
        wallpaperEntity.byline = "Lyubov Popova, 1918";
        wallpaperEntity.imageUri = "imageUri";
        wallpaperEntity.title = "Painterly Architectonic";
        wallpaperEntity.wallpaperId = DEFAULT_WALLPAPER_ID;
        return wallpaperEntity;
    }
}
