package com.yalin.style.data.utils;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author jinyalin
 * @since 2017/4/25.
 */

public class ChecksumUtil {
    public static String getChecksum(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = new FileInputStream(file);
                 DigestInputStream dis = new DigestInputStream(is, md)) {
                byte[] buffer = new byte[2048];
                //noinspection StatementWithEmptyBody
                while (dis.read(buffer) > 0) {

                }
                byte[] digest = dis.getMessageDigest().digest();
                return Base64.encodeToString(digest, Base64.URL_SAFE).trim();
            } catch (Exception e) {
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
