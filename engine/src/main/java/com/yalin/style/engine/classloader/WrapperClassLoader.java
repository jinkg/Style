package com.yalin.style.engine.classloader;

import android.content.Context;
import android.os.Environment;


import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * @author jinyalin
 * @since 2017/7/1.
 */

public class WrapperClassLoader {

    private static ClassLoader getClassLoader(Context context, String componentFilePath) {
        if (context == null) {
            throw new IllegalArgumentException("Context can not be null.");
        }

        synchronized (WrapperClassLoader.class) {
            return new DexClassLoader(componentFilePath,
                    context.getCacheDir().getAbsolutePath(),
                    null, context.getClassLoader());
        }
    }

    public static Class<?> loadClass(Context context, String componentPath, String className)
            throws Exception {
        ClassLoader classLoader = getClassLoader(context, componentPath);
        return classLoader.loadClass(className);
    }
}