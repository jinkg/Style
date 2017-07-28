package com.yalin.style.engine;

import android.content.Context;

import com.yalin.style.engine.classloader.WrapperClassLoader;

/**
 * YaLin
 * On 2017/7/27.
 */

public class WrapperApi {

    public static final String ADVANCE_PROXY_CLASS =
            "com.yalin.component.ProviderImpl";


    private static IProvider getProvider(Context context, String componentPath, String providerName)
            throws Exception {
        synchronized (WrapperApi.class) {
            IProvider provider;
            Class providerClazz = WrapperClassLoader.loadClass(context, componentPath, providerName);
            if (providerClazz != null) {
                provider = (IProvider) providerClazz.newInstance();
                return provider;
            } else {
                throw new IllegalStateException("Load Provider error.");
            }
        }
    }

    public static WallpaperServiceProxy getProxy(Context context,
                                                 String componentPath, String providerName) {
        try {
            IProvider provider = getProvider(context, componentPath, providerName);
            return provider.provideProxy(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
