package com.yalin.style.data.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @author jinyalin
 * @since 2017/4/26.
 */
public class FacetIdUtil {
    static {
        System.loadLibrary("facet_id-lib");
    }

    public static boolean checkCurrentFacetId(Context context) {
        return checkCurrentFacetId(context, getUid(context));
    }

    public static String getFacetId(Context context) {
        return getFacetId(context, getUid(context));
    }

    private static native boolean checkCurrentFacetId(Context context, int uId);

    private static native String getFacetId(Context context, int uId);

    private static int getUid(Context context) {
        if (context != null) {
            return context.getApplicationInfo().uid;
        } else {
            return -1;
        }
    }

    public static String getFacetIdJava(Context context, int callingUid) {
        if (context == null) {
            return null;
        }
        String packageNames[] = context.getPackageManager().getPackagesForUid(callingUid);

        if (packageNames == null) {
            return null;
        }
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageNames[0],
                    PackageManager.GET_SIGNATURES);

            byte[] cert = info.signatures[0].toByteArray();
            InputStream input = new ByteArrayInputStream(cert);

            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate c = (X509Certificate) cf.generateCertificate(input);

            MessageDigest md = MessageDigest.getInstance("SHA1");

            return Base64.encodeToString(md.digest(c.getEncoded()), Base64.DEFAULT);
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }

        return null;
    }

}
