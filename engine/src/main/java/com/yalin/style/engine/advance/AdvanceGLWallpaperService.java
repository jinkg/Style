package com.yalin.style.engine.advance;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.yalin.style.engine.WallpaperServiceProxy;

import org.jetbrains.annotations.NotNull;

/**
 * @author jinyalin
 * @since 2017/7/24.
 */

public class AdvanceGLWallpaperService extends WallpaperServiceProxy {
    public AdvanceGLWallpaperService(@NotNull Context host) {
        super(host);
    }

    @Override
    public Engine onCreateEngine() {
        return new AdvanceEngine();
    }

    private class AdvanceEngine extends ActiveEngine {
        AdvanceRenderer renderer;

        public AdvanceEngine() {
            super();
            renderer = new AdvanceRenderer(AdvanceGLWallpaperService.this);
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            // Add touch events
            setTouchEventsEnabled(true);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            renderer.onTouchEvent(event);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (renderer != null) {
                renderer.release();
            }
            renderer = null;
        }
    }
}
