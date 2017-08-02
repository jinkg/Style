package com.yalin.style.engine;

import android.content.Context;
import android.service.wallpaper.WallpaperService;

import com.yalin.style.engine.component.ComponentContext;

/**
 * YaLin
 * On 2017/7/27.
 */

public class ProxyApi {

    private static IProvider getProvider(ComponentContext context, String providerName)
            throws Exception {
        synchronized (ProxyApi.class) {
            IProvider provider;
            Class providerClazz = context.getClassLoader().loadClass(providerName);
            if (providerClazz != null) {
                provider = (IProvider) providerClazz.newInstance();
                return provider;
            } else {
                throw new IllegalStateException("Load Provider error.");
            }
        }
    }

    public static WallpaperService getProxy(Context context,
                                            String componentPath, String providerName) {
        try {
            ComponentContext componentContext = new ComponentContext(context, componentPath);
            IProvider provider = getProvider(componentContext, providerName);
            return provider.provideProxy(componentContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
