package com.yalin.style.data.utils;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract.Wallpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * YaLin On 2017/1/2.
 */

public class WallpaperFileHelper {

    private static final String TAG = "WallpaperFileHelper";

    public static final String WALLPAPER_FOLDER = "wallpaper";

    public static ParcelFileDescriptor openReadFile(Context context, Uri uri, String mode)
            throws FileNotFoundException {
        LogUtil.D(TAG, "Read file Uri=" + (uri == null ? null : uri.toString()));

        String wallpaperId = Wallpaper.getWallpaperId(uri);

        File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
        if (!directory.exists()) {
            throw new FileNotFoundException("Wallpaper file : "
                    + directory.toString() + " cannot found.");
        }

        File file = new File(directory, generateFileName(wallpaperId));

        return ParcelFileDescriptor.open(file, parseMode(mode));
    }

    public static ParcelFileDescriptor openWriteFile(Context context, Uri uri, String mode)
            throws FileNotFoundException {
        String wallpaperId = Wallpaper.getWallpaperSaveId(uri);
        File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
        if (!directory.exists() && !directory.mkdir()) {
            throw new FileNotFoundException("Wallpaper save dir : "
                    + directory.toString() + " cannot be create.");
        }
        File file = new File(directory, generateFileName(wallpaperId));
        return ParcelFileDescriptor.open(file, parseMode(mode));
    }

    private static String generateFileName(String wallpaperId) {
        return wallpaperId;
    }

    private static int parseMode(String mode) {
        final int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Bad mode '" + mode + "'");
        }
        return modeBits;
    }

    public static boolean copyAssets(Context context, String name, File output) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = context.getAssets().open(name);
            fos = new FileOutputStream(output);
            byte[] buffer = new byte[2048];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteOldFiles(Context context, Set<String> excludeIds) {
        File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
        if (!directory.exists()) {
            return;
        }
        Set<String> namesSet = new HashSet<>();
        for (String wallpaperId : excludeIds) {
            namesSet.add(generateFileName(wallpaperId));
        }
        File[] files = directory.listFiles(fileName ->
                !namesSet.contains(fileName.getName()));
        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static void deleteOldFiles(Context context, String... excludeIds) {
        File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
        if (!directory.exists()) {
            return;
        }
        Set<String> namesSet = new HashSet<>();
        for (String wallpaperId : excludeIds) {
            namesSet.add(generateFileName(wallpaperId));
        }
        File[] files = directory.listFiles(fileName ->
                !namesSet.contains(fileName.getName()));
        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public static boolean ensureChecksumValid(Context context,
                                              String checksum, String wallpaperId) {
        File directory = new File(context.getFilesDir(), WALLPAPER_FOLDER);
        if (!directory.exists()) {
            return false;
        }

        File file = new File(directory, generateFileName(wallpaperId));
        String computedChecksum = ChecksumUtil.getChecksum(file);
        if (TextUtils.equals(checksum, computedChecksum)) {
            return true;
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return false;
    }
}
