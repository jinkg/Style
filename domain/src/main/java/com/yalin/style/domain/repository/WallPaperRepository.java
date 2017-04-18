package com.yalin.style.domain.repository;

import com.yalin.style.domain.WallPaper;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface WallPaperRepository {
    Observable<WallPaper> getWallPaper();
}
