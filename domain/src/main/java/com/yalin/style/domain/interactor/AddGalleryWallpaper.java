package com.yalin.style.domain.interactor;

import com.yalin.style.domain.GalleryWallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.repository.SourcesRepository;

import java.util.List;

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
                .addGalleryWallpaperUris(params.galleryWallpaperUris);
    }

    public static final class Params {

        private final List<GalleryWallpaper> galleryWallpaperUris;

        private Params(List<GalleryWallpaper> galleryWallpapers) {
            this.galleryWallpaperUris = galleryWallpapers;
        }

        public static Params addGalleryWallpaperUris(List<GalleryWallpaper> galleryWallpapers) {
            return new Params(galleryWallpapers);
        }
    }
}
