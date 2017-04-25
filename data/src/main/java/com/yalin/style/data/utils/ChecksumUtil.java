package com.yalin.style.data.utils;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class ChecksumUtil {
    public static String getChecksum(File file) {
        MessageDigest md;
        InputStream is = null;
        try {
            md = MessageDigest.getInstance("MD5");
            is = new FileInputStream(file);
            byte[] digest = md.digest();
            return Base64.encodeToString(digest, Base64.URL_SAFE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
