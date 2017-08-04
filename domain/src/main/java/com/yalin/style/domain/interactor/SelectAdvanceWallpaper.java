package com.yalin.style.domain.interactor;

import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.SerialThreadExecutor;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/7/31.
 */

public class SelectAdvanceWallpaper extends UseCase<Boolean, SelectAdvanceWallpaper.Params> {
    private SourcesRepository sourcesRepository;

    @Inject
    public SelectAdvanceWallpaper(ThreadExecutor threadExecutor,
                                  SerialThreadExecutor serialThreadExecutor,
                                  PostExecutionThread postExecutionThread,
                                  SourcesRepository sourcesRepository) {
        super(threadExecutor, serialThreadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Boolean> buildUseCaseObservable(SelectAdvanceWallpaper.Params params) {
        return sourcesRepository.getWallpaperRepository()
                .selectAdvanceWallpaper(params.wallpaperId, params.tempSelect);
    }

    public static final class Params {
        private final String wallpaperId;
        private final boolean tempSelect;

        private Params(String wallpaperId, boolean tempSelect) {
            this.wallpaperId = wallpaperId;
            this.tempSelect = tempSelect;
        }

        public static Params selectWallpaper(String wallpaperId, boolean tempSelect) {
            return new Params(wallpaperId, tempSelect);
        }
    }
}