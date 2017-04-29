package com.yalin.style.data.repository.datasource.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.yalin.style.data.log.LogUtil;

/**
 * YaLin 2017/1/3.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    public static final String SYNC_MANUALLY = "syn_manually";

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                LogUtil.F(TAG, "Uncaught sync exception, suppressing UI in release build.",
                        throwable));

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        LogUtil.F(TAG, "PerformSync.");
        if (extras.getBoolean(SYNC_MANUALLY)) {
            LogUtil.D(TAG, "Manually sync.");
        }
        new SyncHelper(mContext).performSync(syncResult, extras);
    }
}
