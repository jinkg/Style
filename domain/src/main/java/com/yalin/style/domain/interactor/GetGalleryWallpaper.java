package com.yalin.style.domain.interactor;

import com.yalin.style.domain.GalleryWallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import java.util.Set;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/5/24.
 */
public class GetGalleryWallpaper extends UseCase<Set<GalleryWallpaper>, Void> {
    private SourcesRepository sourcesRepository;

    public GetGalleryWallpaper(ThreadExecutor threadExecutor,
                               PostExecutionThread postExecutionThread,
                               SourcesRepository sourcesRepository) {
        super(threadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Set<GalleryWallpaper>> buildUseCaseObservable(Void aVoid) {
        return sourcesRepository.getWallpaperRepository().getGalleryWallpapers();
    }
}
