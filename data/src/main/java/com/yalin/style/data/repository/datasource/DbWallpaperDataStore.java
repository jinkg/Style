package com.yalin.style.data.repository.datasource;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.yalin.style.data.cache.WallpaperCache;
import com.yalin.style.data.entity.WallpaperEntity;
import com.yalin.style.data.exception.LikeException;
import com.yalin.style.data.lock.LikeWallpaperLock;
import com.yalin.style.data.lock.OpenInputStreamLock;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract;

import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class DbWallpaperDataStore implements WallpaperDataStore {

    private static final String TAG = "DbWallpaperDataStore";

    public static final String DEFAULT_WALLPAPER_ID = "-1";

    private final Context context;

    private final WallpaperCache wallpaperCache;
    private final OpenInputStreamLock openInputStreamLock;
    private final LikeWallpaperLock likeWallpaperLock;

    public DbWallpaperDataStore(Context context, WallpaperCache wallpaperCache,
                                OpenInputStreamLock openInputStreamLock,
                                LikeWallpaperLock likeWallpaperLock) {
        this.context = context;
        this.wallpaperCache = wallpaperCache;
        this.openInputStreamLock = openInputStreamLock;
        this.likeWallpaperLock = likeWallpaperLock;
    }

    @Override
    public Observable<WallpaperEntity> getWallPaperEntity() {
        return createEntitiesObservable().doOnNext(wallpaperCache::put).map(Queue::peek);
    }

    @Override
    public Observable<WallpaperEntity> switchWallPaperEntity() {
        return getWallPaperEntity();
    }

    @Override
    public Observable<InputStream> openInputStream(String wallpaperId) {
        return Observable.create(emitter -> {
            try {
                openInputStreamLock.obtain();
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
            } finally {
                openInputStreamLock.release();
            }
        });
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        return Observable.create(emitter -> {
            Cursor cursor = null;
            int count = 0;
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(StyleContract.Wallpaper.CONTENT_URI,
                    null, null, null, null);
            if (cursor != null) {
                // contain default
                count = cursor.getCount() + 1;
                cursor.close();
            }
            emitter.onNext(count);
            emitter.onComplete();
        });
    }

    @Override
    public Observable<Boolean> likeWallpaper(String wallpaperId) {
        if (!likeWallpaperLock.obtain()) {
            return Observable.create(emitter -> emitter.onError(new LikeException()));
        }
        wallpaperCache.likeWallpaper(wallpaperId);
        return Observable.create(emitter -> {
            Cursor cursor = null;
            try {
                ContentResolver contentResolver = context.getContentResolver();
                cursor = contentResolver.query(StyleContract.Wallpaper.buildWallpaperUri(wallpaperId),
                        null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    WallpaperEntity entity = WallpaperEntity.readEntityFromCursor(cursor);
                    entity.liked = !entity.liked;
                    int columnCount = contentResolver
                            .update(StyleContract.Wallpaper.buildWallpaperLikeUri(wallpaperId),
                                    buildKeepContentValue(entity), null, null);
                    if (columnCount > 0) {
                        emitter.onNext(entity.liked);
                    } else {
                        throw new LikeException();
                    }
                } else {
                    throw new LikeException();
                }
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                likeWallpaperLock.release();
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    private Observable<Queue<WallpaperEntity>> createEntitiesObservable() {
        return Observable.create(emitter -> {
            Cursor cursor = null;
            Queue<WallpaperEntity> validWallpapers = new LinkedList<>();
            try {
                ContentResolver contentResolver = context.getContentResolver();
                cursor = contentResolver.query(Wallpaper.CONTENT_URI,
                        null, null, null, null);
                validWallpapers.addAll(WallpaperEntity.readCursor(context, cursor));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            // from db all wallpaper can be liked
            for (WallpaperEntity entity : validWallpapers) {
                entity.canLike = true;
            }

            validWallpapers.add(buildDefaultWallpaper());

            emitter.onNext(validWallpapers);
            emitter.onComplete();
        });
    }


    public static WallpaperEntity buildDefaultWallpaper() {
        WallpaperEntity wallpaperEntity = new WallpaperEntity();
        wallpaperEntity.id = -1;
        wallpaperEntity.attribution = "kinglloy.com";
        wallpaperEntity.byline = "Lyubov Popova, 1918";
        wallpaperEntity.imageUri = "imageUri";
        wallpaperEntity.title = "Painterly Architectonic";
        wallpaperEntity.wallpaperId = DEFAULT_WALLPAPER_ID;
        wallpaperEntity.liked = false;
        wallpaperEntity.isDefault = true;
        wallpaperEntity.canLike = false;
        return wallpaperEntity;
    }

    private ContentValues buildKeepContentValue(WallpaperEntity entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Wallpaper.COLUMN_NAME_LIKED, entity.liked ? 1 : 0);
        return contentValues;
    }
}
