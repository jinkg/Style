package com.yalin.style.data.utils;


import android.content.Context;

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
}
