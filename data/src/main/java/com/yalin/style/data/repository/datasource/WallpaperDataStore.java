package com.yalin.style.data.repository.datasource;

import android.content.Context;

import com.yalin.style.data.entity.SourceEntity;
import com.yalin.style.data.entity.WallpaperEntity;

import java.io.InputStream;
import java.util.List;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface WallpaperDataStore {

    Observable<WallpaperEntity> getWallPaperEntity();

    Observable<WallpaperEntity> switchWallPaperEntity();

    Observable<InputStream> openInputStream(String wallpaperId);

    Observable<Integer> getWallpaperCount();

    Observable<Boolean> likeWallpaper(String wallpaperId);

    Observable<List<SourceEntity>> getSources(Context context);
}