package com.yalin.style.domain.interactor;

import com.yalin.style.domain.GalleryWallpaper;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.interactor.RemoveGalleryWallpaper.Params;
import com.yalin.style.domain.repository.SourcesRepository;
import io.reactivex.Observable;
import java.util.List;
import javax.inject.Inject;

/**
 * YaLin
 * On 2017/5/26.
 */

public class RemoveGalleryWallpaper extends UseCase<Boolean, Params> {

  private SourcesRepository sourcesRepository;

  @Inject
  public RemoveGalleryWallpaper(ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread, SourcesRepository sourcesRepository) {
    super(threadExecutor, postExecutionThread);
    this.sourcesRepository = sourcesRepository;
  }

  @Override
  Observable<Boolean> buildUseCaseObservable(Params params) {
    return sourcesRepository.getWallpaperRepository()
        .removeGalleryWallpaperUris(params.galleryWallpaperUris);
  }

  public static final class Params {

    private final List<GalleryWallpaper> galleryWallpaperUris;

    private Params(List<GalleryWallpaper> galleryWallpaperUris) {
      this.galleryWallpaperUris = galleryWallpaperUris;
    }

    public static RemoveGalleryWallpaper.Params removeGalleryWallpaperUris(
        List<GalleryWallpaper> customWallpapers) {
      return new RemoveGalleryWallpaper.Params(customWallpapers);
    }
  }
}
