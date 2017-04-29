package com.yalin.style.render;

import android.content.Context;

import com.yalin.style.data.log.LogUtil;
import com.yalin.style.domain.Wallpaper;
import com.yalin.style.domain.interactor.DefaultObserver;
import com.yalin.style.domain.interactor.GetWallpaper;
import com.yalin.style.domain.interactor.OpenWallpaperInputStream;
import com.yalin.style.mapper.WallpaperItemMapper;
import com.yalin.style.model.WallpaperItem;

import java.io.InputStream;

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
    private final OpenWallpaperInputStream openWallpaperInputStreamUseCase;
    private final WallpaperItemMapper wallpaperItemMapper;
    private final WallpaperRefreshObserver wallpaperRefreshObserver;

    @Inject
    public RenderController(Context context, GetWallpaper getWallpaperUseCase,
                            OpenWallpaperInputStream openWallpaperInputStream,
                            WallpaperItemMapper wallpaperItemMapper) {
        mContext = context;

        this.wallpaperItemMapper = wallpaperItemMapper;
        wallpaperRefreshObserver = new WallpaperRefreshObserver();
        this.getWallpaperUseCase = getWallpaperUseCase;
        this.getWallpaperUseCase.registerObserver(wallpaperRefreshObserver);
        this.openWallpaperInputStreamUseCase = openWallpaperInputStream;
    }

    public void setComponent(StyleBlurRenderer renderer,Callbacks callbacks){
        this.mRenderer = renderer;
        this.mCallbacks = callbacks;
//        reloadCurrentWallpaper();
    }

    public void destroy() {
        if (mQueuedBitmapRegionLoader != null) {
            mQueuedBitmapRegionLoader.destroy();
        }
        getWallpaperUseCase.unregisterObserver(wallpaperRefreshObserver);
    }

    private BitmapRegionLoader createBitmapRegionLoader(InputStream inputStream)
            throws Exception {
        BitmapRegionLoader bitmapRegionLoader
                = BitmapRegionLoader.newInstance(inputStream);
        if (bitmapRegionLoader == null) {
            throw new IllegalStateException("Bitmap region loader create failed.");
        }
        return bitmapRegionLoader;
    }

    public void reloadCurrentWallpaper() {
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
            openWallpaperInputStreamUseCase.execute(new WallpaperInputStreamObserver(),
                    OpenWallpaperInputStream.Params.openInputStream(wallpaperItem.wallpaperId));
        }

        @Override
        public void onError(Throwable exception) {
            LogUtil.E(TAG, "Load wallpaper failed.", exception);
        }
    }


    private final class WallpaperInputStreamObserver extends DefaultObserver<InputStream> {
        @Override
        public void onNext(InputStream inputStream) {
            BitmapRegionLoader bitmapRegionLoader = null;
            try {
                bitmapRegionLoader = createBitmapRegionLoader(inputStream);
            } catch (Exception e) {
                onError(e);
            }
            LogUtil.D(TAG, "Create bitmap region loader success.");
            setBitmapRegionLoader(bitmapRegionLoader);
        }

        @Override
        public void onError(Throwable exception) {
            LogUtil.E(TAG, "Open input stream failed. ", exception);
        }
    }

    private final class WallpaperRefreshObserver extends DefaultObserver<Void> {
        @Override
        public void onComplete() {
            LogUtil.D(TAG, "Wallpaper update,reload wallpaper.");
            reloadCurrentWallpaper();
        }
    }

    public interface Callbacks {

        void queueEventOnGlThread(Runnable runnable);

        void requestRender();
    }
}
