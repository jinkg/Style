package com.yalin.style.domain.interactor;

import com.fernandocejas.arrow.checks.Preconditions;
import com.yalin.style.domain.executor.PostExecutionThread;
import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.interactor.KeepWallpaper.Params;
import com.yalin.style.domain.repository.WallpaperRepository;
import io.reactivex.Observable;
import javax.inject.Inject;

/**
 * YaLin
 * On 2017/4/30.
 */

public class KeepWallpaper extends UseCase<Boolean, Params> {

  private WallpaperRepository wallpaperRepository;

  @Inject
  public KeepWallpaper(ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread,
      WallpaperRepository wallpaperRepository) {
    super(threadExecutor, postExecutionThread);
    this.wallpaperRepository = wallpaperRepository;
  }


  @Override
  Observable<Boolean> buildUseCaseObservable(KeepWallpaper.Params params) {
    Preconditions.checkNotNull(params);
    return wallpaperRepository.keepWallpaper(params.wallpaperId);
  }

  public static final class Params {

    private final String wallpaperId;

    private Params(String wallpaperId) {
      this.wallpaperId = wallpaperId;
    }

    public static KeepWallpaper.Params keepWallpaper(String wallpaperId) {
      return new KeepWallpaper.Params(wallpaperId);
    }
  }
}
