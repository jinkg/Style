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

public class RefreshWallpapers extends UseCase<Void, Void> {
    private WallpaperRepository wallpaperRepository;

    @Inject
    public RefreshWallpapers(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                             WallpaperRepository wallpaperRepository) {
        super(threadExecutor, postExecutionThread);
        this.wallpaperRepository = wallpaperRepository;
    }

    @Override
    Observable<Void> buildUseCaseObservable(Void aVoid) {
        return wallpaperRepository.refreshWallpapers();
    }
}
