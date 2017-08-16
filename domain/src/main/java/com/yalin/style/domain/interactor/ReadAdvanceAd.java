package com.yalin.style.domain.interactor;

import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.SerialThreadExecutor;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/8/16.
 */

public class ReadAdvanceAd extends UseCase<Boolean, ReadAdvanceAd.Params> {

    private SourcesRepository sourcesRepository;

    @Inject
    public ReadAdvanceAd(ThreadExecutor threadExecutor,
                         SerialThreadExecutor serialThreadExecutor,
                         PostExecutionThread postExecutionThread,
                         SourcesRepository sourcesRepository) {
        super(threadExecutor, serialThreadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Boolean> buildUseCaseObservable(Params params) {
        return sourcesRepository.getWallpaperRepository()
                .readAdvanceAd(params.wallpaperId);
    }

    public static final class Params {

        private final String wallpaperId;

        private Params(String wallpaperId) {
            this.wallpaperId = wallpaperId;
        }

        public static Params read(String wallpaperId) {
            return new Params(wallpaperId);
        }
    }
}
