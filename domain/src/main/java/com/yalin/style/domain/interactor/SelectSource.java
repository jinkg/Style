package com.yalin.style.domain.interactor;

import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.SerialThreadExecutor;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public class SelectSource extends UseCase<Boolean, SelectSource.Params> {
    private SourcesRepository sourcesRepository;

    @Inject
    public SelectSource(ThreadExecutor threadExecutor,
                        SerialThreadExecutor serialThreadExecutor,
                        PostExecutionThread postExecutionThread,
                        SourcesRepository sourcesRepository) {
        super(threadExecutor, serialThreadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Boolean> buildUseCaseObservable(Params params) {
        return sourcesRepository.selectSource(params.sourceId);
    }

    public static final class Params {

        private final int sourceId;

        private Params(int sourceId) {
            this.sourceId = sourceId;
        }

        public static Params selectSource(int sourceId) {
            return new Params(sourceId);
        }
    }

}
