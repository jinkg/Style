package com.yalin.style.domain.repository;

import com.yalin.style.domain.GalleryWallpaper;
import com.yalin.style.domain.Wallpaper;

import java.io.InputStream;
import java.util.List;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface WallpaperRepository {

    Observable<Wallpaper> getWallpaper();

    Observable<Wallpaper> switchWallpaper();

    Observable<InputStream> openInputStream(String wallpaperId);

    Observable<Integer> getWallpaperCount();

    Observable<Boolean> likeWallpaper(String wallpaperId);

    Observable<Boolean> addCustomWallpaperUris(List<GalleryWallpaper> uris);

    Observable<List<GalleryWallpaper>> getGalleryWallpapers();
}
