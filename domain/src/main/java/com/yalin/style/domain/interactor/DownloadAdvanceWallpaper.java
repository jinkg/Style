package com.yalin.style.domain.interactor;

import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.SerialThreadExecutor;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/8/11.
 */

public class DownloadAdvanceWallpaper extends UseCase<Long, DownloadAdvanceWallpaper.Params> {
    private SourcesRepository sourcesRepository;

    @Inject
    public DownloadAdvanceWallpaper(ThreadExecutor threadExecutor,
                                    SerialThreadExecutor serialThreadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    SourcesRepository sourcesRepository) {
        super(threadExecutor, serialThreadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Long> buildUseCaseObservable(Params params) {
        return sourcesRepository.getWallpaperRepository()
                .downloadAdvanceWallpaper(params.wallpaperId);
    }

    public static final class Params {

        private final String wallpaperId;

        private Params(String wallpaperId) {
            this.wallpaperId = wallpaperId;
        }

        public static Params download(String wallpaperId) {
            return new Params(wallpaperId);
        }
    }
}