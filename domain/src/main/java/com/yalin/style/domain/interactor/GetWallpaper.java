package com.yalin.style.domain.interactor;

import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.WallpaperRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class GetWallpaper extends UseCase<Wallpaper, Void> {
    private WallpaperRepository wallpaperRepository;

    @Inject
    public GetWallpaper(ThreadExecutor threadExecutor,
                        PostExecutionThread postExecutionThread,
                        WallpaperRepository wallpaperRepository) {
        super(threadExecutor, postExecutionThread);
        this.wallpaperRepository = wallpaperRepository;
    }

    @Override
    Observable<Wallpaper> buildUseCaseObservable(Void a) {
        return wallpaperRepository.getWallpaper();
    }
}
