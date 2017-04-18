package com.yalin.style.data.repository.datasource.sync;


import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.yalin.style.data.BuildConfig;
import com.yalin.style.data.Config;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.provider.StyleContract;
import java.io.IOException;

/**
 * YaLin 2017/1/3.
 */

public class SyncHelper {

  private static final String TAG = "SyncHelper";
  private final Context mContext;

  private StyleDataHandler mDataHandler;

  public SyncHelper(Context context) {
    mContext = context;
    mDataHandler = new StyleDataHandler(mContext);
  }

  public boolean performSync(SyncResult syncResult, Bundle extras) {
    try {
      doStyleSync();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }

  private boolean doStyleSync() throws IOException {
    if (!isOnline()) {
      LogUtil.d(TAG, "Not attempting remote sync because device is OFFLINE");
      return false;
    }

    LogUtil.d(TAG, "Starting remote sync.");

    String data = new RemoteStyleDataFetcher(mContext).fetchStyleDataIfNewer();
    if (!TextUtils.isEmpty(data)) {
      mDataHandler.applyStyleData(new String[]{data});
    }
    return true;
  }

  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) mContext
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo() != null &&
        cm.getActiveNetworkInfo().isConnectedOrConnecting();
  }

  public static void updateSyncInterval(final Context context) {
    Account account = com.yalin.style.data.repository.datasource.sync.account.Account.getAccount();
    LogUtil.d(TAG, "Checking sync interval");
    long recommended = calculateRecommendedSyncInterval(context);
    LogUtil.d(TAG, "Setting up sync for account, interval " + recommended + "ms");
    ContentResolver.setIsSyncable(account, StyleContract.AUTHORITY, 1);
    ContentResolver.setSyncAutomatically(account, StyleContract.AUTHORITY, true);
    ContentResolver
        .addPeriodicSync(account, StyleContract.AUTHORITY, new Bundle(), recommended / 1000L);

  }

  private static long calculateRecommendedSyncInterval(final Context context) {
    if (BuildConfig.DEBUG) {
      return Config.DEBUG_AUTO_SYNC_INTERVAL_LONG;
    } else {
      return Config.AUTO_SYNC_INTERVAL_LONG;
    }
  }
}
