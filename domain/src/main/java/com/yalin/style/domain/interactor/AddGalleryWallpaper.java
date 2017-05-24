package com.yalin.style.domain.interactor;

import com.yalin.style.domain.GalleryWallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/5/24.
 */

public class AddGalleryWallpaper extends UseCase<Boolean, AddGalleryWallpaper.Params> {
    private SourcesRepository sourcesRepository;

    @Inject
    public AddGalleryWallpaper(ThreadExecutor threadExecutor,
                               PostExecutionThread postExecutionThread,
                               SourcesRepository sourcesRepository) {
        super(threadExecutor, postExecutionThread);
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    Observable<Boolean> buildUseCaseObservable(Params params) {
        return sourcesRepository.getWallpaperRepository()
                .addCustomWallpaperUris(params.customWallpaperUris);
    }

    public static final class Params {

        private final Set<GalleryWallpaper> customWallpaperUris;

        private Params(Set<GalleryWallpaper> customWallpapers) {
            this.customWallpaperUris = customWallpapers;
        }

        public static Params addCustomWallpaperUris(Set<GalleryWallpaper> customWallpapers) {
            return new Params(customWallpapers);
        }
    }
}
