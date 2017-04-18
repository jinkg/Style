package com.yalin.style.data.repository.datasource.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.yalin.style.data.BuildConfig;
import com.yalin.style.data.log.LogUtil;

/**
 * YaLin 2017/1/3.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

  private static final String TAG = "SyncAdapter";

  private final Context mContext;

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    mContext = context;

    if (!BuildConfig.DEBUG) {
      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
          LogUtil.f(TAG, "Uncaught sync exception, suppressing UI in release build.",
              throwable);
        }
      });
    }
  }

  @Override
  public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {
    LogUtil.d(TAG, "PerformSync");

    new SyncHelper(mContext).performSync(syncResult, extras);
  }
}
