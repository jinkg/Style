package com.yalin.style.engine.component;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

/**
 * @author jinyalin
 * @since 2017/8/9.
 */

public class StyleClassLoader extends DexClassLoader {
    private static final String TAG = "StyleClassLoader";

    private Context context;
    private String dexPath;

    private String nativePath;

    public StyleClassLoader(Context context, String dexPath, String optimizedDirectory,
                            String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        this.context = context;
        this.dexPath = dexPath;
    }

    @Override
    public String findLibrary(String name) {
        String soName = getNativeFileName(name);
        maybeCopyNativeLib(soName);
        File targetFile = new File(getNativeDir(), soName);
        return targetFile.exists() ? targetFile.getAbsolutePath() : null;
    }

    private File getNativeDir() {
        if (TextUtils.isEmpty(nativePath)) {
            File cacheDir = context.getCacheDir();
            File nativeDir = new File(cacheDir.getParent(), "lib");
            nativeDir.mkdirs();
            nativePath = nativeDir.getAbsolutePath();
        }
        return new File(nativePath);
    }

    private String getNativeFileName(String libName) {
        String[] tmp = dexPath.split("/");
        String pluginName = tmp[tmp.length - 1].split("\\.")[0];
        return "plugin_" + pluginName + "_" + libName + ".so";
    }

    private void maybeCopyNativeLib(String libName) {
        try {

            File soFile = new File(getNativeDir(), libName);
            if (soFile.exists()) {
                return;
            }

            String cpuArch;
            if (Build.VERSION.SDK_INT >= 21) {
                cpuArch = Build.SUPPORTED_ABIS[0];
            } else {
                cpuArch = Build.CPU_ABI;
            }
            boolean findSo = false;

            ZipFile zipfile = new ZipFile(dexPath);
            ZipEntry entry;
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory())
                    continue;
                if (entry.getName().endsWith(".so") && entry.getName().contains("lib/" + cpuArch)) {
                    findSo = true;
                    break;
                }
            }
            e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".so"))
                    continue;
                if ((findSo && entry.getName().contains("lib/" + cpuArch))
                        || (!findSo && entry.getName().contains("lib/armeabi/"))) {
                    File libFile = new File(getNativeDir().getAbsolutePath() + File.separator + libName);
                    if (libFile.exists()) {
                        // check version
                    }
                    FileOutputStream fos = new FileOutputStream(libFile);
                    Log.d(TAG, "copy so " + entry.getName() + " of " + cpuArch);
                    copySo(zipfile.getInputStream(entry), fos);
                    break;
                }

            }

            zipfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copySo(InputStream input, OutputStream output) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(input);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
        int count;
        byte data[] = new byte[8192];
        while ((count = bufferedInput.read(data, 0, 8192)) != -1) {
            bufferedOutput.write(data, 0, count);
        }
        bufferedOutput.flush();
        bufferedOutput.close();
        output.close();
        bufferedInput.close();
        input.close();
    }
}
