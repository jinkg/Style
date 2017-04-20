package com.yalin.style.presenter;

import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.interactor.DefaultObserver;
import com.yalin.style.domain.interactor.GetWallpaper;
import com.yalin.style.injection.PerActivity;
import com.yalin.style.mapper.WallpaperItemMapper;
import com.yalin.style.model.WallpaperItem;
import com.yalin.style.view.WallpaperDetailView;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */
@PerActivity
public class WallpaperDetailPresenter implements Presenter {
    private final GetWallpaper getWallpaperUseCase;
    private final WallpaperItemMapper wallpaperItemMapper;

    private WallpaperDetailView wallpaperDetailView;

    @Inject
    public WallpaperDetailPresenter(GetWallpaper getWallpaperUseCase,
                                    WallpaperItemMapper itemMapper) {
        this.getWallpaperUseCase = getWallpaperUseCase;
        this.wallpaperItemMapper = itemMapper;
    }

    public void setView(WallpaperDetailView wallpaperDetailView) {
        this.wallpaperDetailView = wallpaperDetailView;
    }

    public void initialize() {
        getWallpaperUseCase.execute(new WallpaperObserver(), null);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        getWallpaperUseCase.dispose();
        wallpaperDetailView = null;
    }

    private void showWallpaperDetailInView(Wallpaper wallpaper) {
        final WallpaperItem wallpaperItem = wallpaperItemMapper.transform(wallpaper);
        wallpaperDetailView.renderWallpaper(wallpaperItem);
    }

    private final class WallpaperObserver extends DefaultObserver<Wallpaper> {
        @Override
        public void onNext(Wallpaper wallpaper) {
            showWallpaperDetailInView(wallpaper);
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable exception) {
            wallpaperDetailView.showError(exception.getMessage());
        }
    }
}
