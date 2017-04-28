package com.yalin.style.domain.interactor;

import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.WallpaperRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/28.
 */

public class SwitchWallpaper extends UseCase<Wallpaper, Void> {
    private WallpaperRepository wallpaperRepository;

    @Inject
    public SwitchWallpaper(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           WallpaperRepository wallpaperRepository) {
        super(threadExecutor, postExecutionThread);
        this.wallpaperRepository = wallpaperRepository;
    }

    @Override
    Observable<Wallpaper> buildUseCaseObservable(Void aVoid) {
        return wallpaperRepository.switchWallpaper();
    }
}
