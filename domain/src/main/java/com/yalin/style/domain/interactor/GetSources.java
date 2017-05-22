package com.yalin.style.domain.interactor;

import com.yalin.style.domain.Source;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.WallpaperRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/5/22.
 */

public class GetSources extends UseCase<List<Source>, Void> {
    private WallpaperRepository wallpaperRepository;

    @Inject
    public GetSources(ThreadExecutor threadExecutor,
                      PostExecutionThread postExecutionThread,
                      WallpaperRepository wallpaperRepository) {
        super(threadExecutor, postExecutionThread);
        this.wallpaperRepository = wallpaperRepository;
    }

    @Override
    Observable<List<Source>> buildUseCaseObservable(Void a) {
        return wallpaperRepository.getSources();
    }
}
