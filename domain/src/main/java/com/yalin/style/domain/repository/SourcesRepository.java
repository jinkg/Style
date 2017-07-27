package com.yalin.style.domain.repository;

import com.yalin.style.domain.Source;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public interface SourcesRepository {
    int SOURCE_ID_STYLE = 0;
    int SOURCE_ID_CUSTOM = 1;
    int SOURCE_ID_ADVANCE = 2;

    Observable<List<Source>> getSources();

    Observable<Boolean> selectSource(int sourceId);

    WallpaperRepository getWallpaperRepository();

    int getSelectedSource();

}
