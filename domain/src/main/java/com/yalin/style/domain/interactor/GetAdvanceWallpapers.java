package com.yalin.style.domain.interactor;

import com.yalin.style.domain.AdvanceWallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.SerialThreadExecutor;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/7/28.
 */

public class GetAdvanceWallpapers extends UseCase<List<AdvanceWallpaper>, Void> {
    private SourcesRepository sourcesRepository;

    @Inject
    public GetAdvanceWallpapers(ThreadExecutor threadExecutor,
                                SerialThreadExecutor serialThreadExecutor,
                                PostExecutionThread postExecutionThread,
                                SourcesRepository sourcesRepository) {
        super(threadExecutor, serialThreadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<List<AdvanceWallpaper>> buildUseCaseObservable(Void aVoid) {
        return sourcesRepository.getWallpaperRepository().getAdvanceWallpapers();
    }
}
