package com.yalin.style.data.repository.datasource.provider;

import android.net.Uri;

/**
 * YaLin 2017/1/3.
 */

public class StyleContractHelper {

  private static final String QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER = "callerIsSyncAdapter";

  public static Uri setUriAsCalledFromSyncAdapter(Uri uri) {
    return uri.buildUpon().appendQueryParameter(QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER, "true")
        .build();
  }

  public static boolean isUriCalledFromSyncAdapter(Uri uri) {
    return uri.getBooleanQueryParameter(QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER, false);
  }
}
