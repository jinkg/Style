package com.yalin.style.domain.interactor;

import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/28.
 */

public class SwitchWallpaper extends UseCase<Wallpaper, Void> {
    private SourcesRepository sourcesRepository;

    @Inject
    public SwitchWallpaper(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           SourcesRepository sourcesRepository) {
        super(threadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Wallpaper> buildUseCaseObservable(Void aVoid) {
        return sourcesRepository.getWallpaperRepository().switchWallpaper();
    }
}
