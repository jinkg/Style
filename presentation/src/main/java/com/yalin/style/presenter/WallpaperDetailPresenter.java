package com.yalin.style.presenter;

import android.content.Intent;

import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.interactor.DefaultObserver;
import com.yalin.style.domain.interactor.GetWallpaper;
import com.yalin.style.domain.interactor.GetWallpaperCount;
import com.yalin.style.domain.interactor.SwitchWallpaper;
import com.yalin.style.event.WallpaperSwitchEvent;
import com.yalin.style.injection.PerActivity;
import com.yalin.style.mapper.WallpaperItemMapper;
import com.yalin.style.model.WallpaperItem;
import com.yalin.style.view.WallpaperDetailView;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/4/20.
 */
@PerActivity
public class WallpaperDetailPresenter implements Presenter {
    private final GetWallpaper getWallpaperUseCase;
    private final GetWallpaperCount getWallpaperCountUseCase;
    private final SwitchWallpaper switchWallpaperUseCase;
    private final WallpaperItemMapper wallpaperItemMapper;

    private WallpaperItem currentShowItem;

    private WallpaperDetailView wallpaperDetailView;

    private WallpaperRefreshObserver wallpaperRefreshObserver = new WallpaperRefreshObserver();

    @Inject
    public WallpaperDetailPresenter(GetWallpaper getWallpaperUseCase,
                                    GetWallpaperCount getWallpaperCountUseCase,
                                    SwitchWallpaper switchWallpaperUseCase,
                                    WallpaperItemMapper itemMapper) {
        this.getWallpaperUseCase = getWallpaperUseCase;
        this.getWallpaperCountUseCase = getWallpaperCountUseCase;
        this.switchWallpaperUseCase = switchWallpaperUseCase;
        this.wallpaperItemMapper = itemMapper;

        getWallpaperUseCase.registerObserver(wallpaperRefreshObserver);
    }

    public void setView(WallpaperDetailView wallpaperDetailView) {
        this.wallpaperDetailView = wallpaperDetailView;
    }

    public void initialize() {
        getWallpaperUseCase.execute(new WallpaperObserver(), null);
        getWallpaperCountUseCase.execute(new WallpaperCountObserver(), null);
    }

    public void getNextWallpaper() {
        switchWallpaperUseCase.execute(new WallpaperObserver(true), null);
    }

    public void shareWallpaper() {
        if (currentShowItem == null) {
            return;
        }
        String detailUrl = "www.kinglloy.com";
        String artist = currentShowItem.byline.replaceFirst("\\.\\s*($|\\n).*", "").trim();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My Android wallpaper today is '"
                + currentShowItem.title.trim()
                + "' by " + artist
                + ". #StyleWallpaper\n\n"
                + detailUrl);
        shareIntent = Intent.createChooser(shareIntent, "Share wallpaper");
        wallpaperDetailView.shareWallpaper(shareIntent);
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
        getWallpaperUseCase.unregisterObserver(wallpaperRefreshObserver);
        wallpaperDetailView = null;
    }

    private void showWallpaperDetailInView(Wallpaper wallpaper) {
        final WallpaperItem wallpaperItem = wallpaperItemMapper.transform(wallpaper);
        currentShowItem = wallpaperItem;
        wallpaperDetailView.renderWallpaper(wallpaperItem);
    }

    private void showOrHideNextView(boolean show) {
        wallpaperDetailView.showNextButton(show);
    }

    private final class WallpaperObserver extends DefaultObserver<Wallpaper> {
        private boolean isSwitch;

        public WallpaperObserver() {
            this(false);
        }

        public WallpaperObserver(boolean isSwitch) {
            this.isSwitch = isSwitch;
        }

        @Override
        public void onNext(Wallpaper wallpaper) {
            showWallpaperDetailInView(wallpaper);
        }

        @Override
        public void onComplete() {
            if (isSwitch) {
                EventBus.getDefault().post(new WallpaperSwitchEvent());
            }
        }

        @Override
        public void onError(Throwable exception) {
            wallpaperDetailView.showError(exception.getMessage());
        }
    }

    private final class WallpaperCountObserver extends DefaultObserver<Integer> {
        @Override
        public void onNext(Integer count) {
            showOrHideNextView(count > 1);
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable exception) {
            showOrHideNextView(false);
        }
    }

    private final class WallpaperRefreshObserver extends DefaultObserver<Void> {
        @Override
        public void onComplete() {
            initialize();
        }
    }
}
