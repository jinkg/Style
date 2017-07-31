package com.yalin.style.domain.interactor;

import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.SerialThreadExecutor;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/6/9.
 */

public class SetGalleryUpdateInterval extends UseCase<Boolean, SetGalleryUpdateInterval.Params> {
    private SourcesRepository sourcesRepository;

    @Inject
    public SetGalleryUpdateInterval(ThreadExecutor threadExecutor,
                                    SerialThreadExecutor serialThreadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    SourcesRepository sourcesRepository) {
        super(threadExecutor, serialThreadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Boolean> buildUseCaseObservable(Params params) {
        return sourcesRepository.getWallpaperRepository()
                .setGalleryUpdateInterval(params.intervalMin);
    }

    public static final class Params {
        private final int intervalMin;

        private Params(int intervalMin) {
            this.intervalMin = intervalMin;
        }

        public static Params interval(int intervalMin) {
            return new Params(intervalMin);
        }
    }
}
