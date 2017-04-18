package com.yalin.style.data.repository.datasource.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * YaLin 2017/1/3.
 */

public class SyncService extends Service {

  private static final Object sSyncAdapterLock = new Object();
  private static SyncAdapter sSyncAdapter = null;

  @Override
  public void onCreate() {
    super.onCreate();
    synchronized (sSyncAdapterLock) {
      if (sSyncAdapter == null) {
        sSyncAdapter = new SyncAdapter(getApplicationContext(), false);
      }
    }
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return sSyncAdapter.getSyncAdapterBinder();
  }
}
