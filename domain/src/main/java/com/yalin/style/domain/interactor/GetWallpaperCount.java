package com.yalin.style.domain.interactor;


import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.WallpaperRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/28.
 */

public class GetWallpaperCount extends UseCase<Integer, Void> {
    private WallpaperRepository wallpaperRepository;

    @Inject
    public GetWallpaperCount(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                             WallpaperRepository wallpaperRepository) {
        super(threadExecutor, postExecutionThread);
        this.wallpaperRepository = wallpaperRepository;
    }

    @Override
    Observable<Integer> buildUseCaseObservable(Void aVoid) {
        return wallpaperRepository.getWallpaperCount();
    }
}
