package com.yalin.style.engine.component;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

import com.yalin.style.engine.WallpaperActiveCallback;
import com.yalin.style.engine.resource.ResourcesManager;

import dalvik.system.DexClassLoader;


/**
 * @author jinyalin
 * @since 2017/7/3.
 */

public class ComponentContext extends ContextWrapper implements WallpaperActiveCallback {
    private String componentPath;
    private WallpaperActiveCallback origin;

    public ComponentContext(Context base, String componentPath) {
        super(base.getApplicationContext());
        this.componentPath = componentPath;
        if (base instanceof WallpaperActiveCallback) {
            origin = (WallpaperActiveCallback) base;
        }
    }

    @Override
    public Resources getResources() {
        return ResourcesManager.createResources(getBaseContext(), componentPath);
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClassLoader(componentPath);
    }


    private ClassLoader getClassLoader(String componentFilePath) {
        synchronized (ComponentContext.class) {
            return new DexClassLoader(componentFilePath,
                    getCacheDir().getAbsolutePath(),
                    null, getBaseContext().getClassLoader());
        }
    }

    @Override
    public void onWallpaperActivate() {
        if (origin != null) {
            origin.onWallpaperActivate();
        }
    }

    @Override
    public void onWallpaperDeactivate() {
        if (origin != null) {
            origin.onWallpaperDeactivate();
        }
    }
}

