package com.yalin.style.render;

import android.content.Context;

import com.yalin.style.data.log.LogUtil;
import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.interactor.DefaultObserver;
import com.yalin.style.domain.interactor.GetWallpaper;
import com.yalin.style.mapper.WallpaperItemMapper;
import com.yalin.style.model.WallpaperItem;

import java.io.IOException;

import javax.inject.Inject;

/**
 * YaLin 2016/12/30.
 */

public class RenderController {
    private static final String TAG = "RenderController";

    protected Context mContext;
    protected StyleBlurRenderer mRenderer;
    protected Callbacks mCallbacks;
    protected boolean mVisible;
    private BitmapRegionLoader mQueuedBitmapRegionLoader;

    private final GetWallpaper getWallpaperUseCase;
    private final WallpaperItemMapper wallpaperItemMapper;
    private final WallpaperRefreshObserver wallpaperRefreshObserver;

    @Inject
    public RenderController(Context context, GetWallpaper getWallpaperUseCase,
                            WallpaperItemMapper wallpaperItemMapper) {
        mContext = context;

        this.wallpaperItemMapper = wallpaperItemMapper;
        wallpaperRefreshObserver = new WallpaperRefreshObserver();
        this.getWallpaperUseCase = getWallpaperUseCase;
        this.getWallpaperUseCase.registerObserver(wallpaperRefreshObserver);
    }

    public void setRenderer(StyleBlurRenderer renderer) {
        this.mRenderer = renderer;
    }

    public void setCallbacks(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void destroy() {
        if (mQueuedBitmapRegionLoader != null) {
            mQueuedBitmapRegionLoader.destroy();
        }
        getWallpaperUseCase.unregisterObserver(wallpaperRefreshObserver);
    }

    private BitmapRegionLoader createBitmapRegionLoader(WallpaperItem wallpaperItem) {
        try {
            return BitmapRegionLoader.newInstance(wallpaperItem.inputStream);
        } catch (IOException e) {
            LogUtil.D(TAG, "Could't load wallpaper. " + wallpaperItem.title
                    + " " + e.getMessage());
        }
        return null;
    }

    public void reloadCurrentArtwork() {
        getWallpaperUseCase.execute(new WallpaperItemObserver(), null);
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
        if (visible) {
            mCallbacks.queueEventOnGlThread(new Runnable() {
                @Override
                public void run() {
                    if (mQueuedBitmapRegionLoader != null) {
                        mRenderer.setAndConsumeBitmapRegionLoader(mQueuedBitmapRegionLoader);
                        mQueuedBitmapRegionLoader = null;
                    }
                }
            });
            mCallbacks.requestRender();
        }
    }

    private void setBitmapRegionLoader(final BitmapRegionLoader bitmapRegionLoader) {
        mCallbacks.queueEventOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (mVisible) {
                    mRenderer.setAndConsumeBitmapRegionLoader(bitmapRegionLoader);
                } else {
                    mQueuedBitmapRegionLoader = bitmapRegionLoader;
                }
            }
        });
    }

    private final class WallpaperItemObserver extends DefaultObserver<Wallpaper> {
        @Override
        public void onNext(Wallpaper wallpaper) {
            WallpaperItem wallpaperItem = wallpaperItemMapper.transform(wallpaper);
            final BitmapRegionLoader bitmapRegionLoader
                    = createBitmapRegionLoader(wallpaperItem);
            if (bitmapRegionLoader == null) {
                onError(new NullPointerException("Create bitmapRegionLoader error."));
                return;
            }
            setBitmapRegionLoader(bitmapRegionLoader);
        }
    }

    private final class WallpaperRefreshObserver extends DefaultObserver<Void> {
        @Override
        public void onComplete() {
            reloadCurrentArtwork();
        }
    }

    public interface Callbacks {

        void queueEventOnGlThread(Runnable runnable);

        void requestRender();
    }
}
