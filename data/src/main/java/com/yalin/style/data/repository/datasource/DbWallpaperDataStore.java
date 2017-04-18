package com.yalin.style.data.repository.datasource;

import android.content.Context;

import com.yalin.style.data.entity.WallpaperEntity;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class DbWallpaperDataStore implements WallpaperDataStore {

    private Context context;

    public DbWallpaperDataStore(Context context) {
        this.context = context;
    }

    @Override
    public Observable<WallpaperEntity> getWallPaperEntity() {
        return Observable.create(emitter -> {
            WallpaperEntity wallpaperEntity = new WallpaperEntity();
            wallpaperEntity.attribution = "attribution";
            wallpaperEntity.byline = "byline";
            wallpaperEntity.imageUri = "imageUri";
            wallpaperEntity.title = "demo";
            wallpaperEntity.wallpaperId = "10";
            wallpaperEntity.inputStream = context.getAssets().open("starrynight.jpg");

            emitter.onNext(wallpaperEntity);
            emitter.onComplete();
        });
    }
}
