package com.yalin.style.domain.interactor;

import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/6/9.
 */

public class GetGalleryUpdateInterval extends UseCase<Integer, Void> {

    private SourcesRepository sourcesRepository;

    @Inject
    public GetGalleryUpdateInterval(ThreadExecutor threadExecutor,
                                    PostExecutionThread postExecutionThread,
                                    SourcesRepository sourcesRepository) {
        super(threadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Integer> buildUseCaseObservable(Void aVoid) {
        return sourcesRepository.getWallpaperRepository().getGalleryUpdateInterval();
    }
}
