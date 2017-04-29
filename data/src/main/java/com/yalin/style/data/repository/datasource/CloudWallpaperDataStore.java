package com.yalin.style.data.repository.datasource;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.yalin.style.data.R;
import com.yalin.style.data.cache.WallpaperCache;
import com.yalin.style.data.entity.WallpaperEntity;
import com.yalin.style.data.exception.NetworkConnectionException;
import com.yalin.style.data.exception.ResyncException;
import com.yalin.style.data.log.LogUtil;
import com.yalin.style.data.repository.datasource.sync.SyncAdapter;
import com.yalin.style.data.repository.datasource.sync.account.Account;
import com.yalin.style.data.utils.NetworkUtil;

import java.io.InputStream;

import io.reactivex.Observable;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public class CloudWallpaperDataStore implements WallpaperDataStore {
    private static final String TAG = "CloudWallpaperDataStore";

    private final Context context;

    private final WallpaperCache wallpaperCache;

    public CloudWallpaperDataStore(Context context, WallpaperCache wallpaperCache) {
        this.context = context;
        this.wallpaperCache = wallpaperCache;
    }

    @Override
    public Observable<WallpaperEntity> getWallPaperEntity() {
        throw new UnsupportedOperationException("Cache data store not support get entity.");
    }

    @Override
    public Observable<WallpaperEntity> switchWallPaperEntity() {
        throw new UnsupportedOperationException("Cache data store not support switch.");

    }

    @Override
    public Observable<InputStream> openInputStream(String wallpaperId) {
        throw new UnsupportedOperationException("Cache data store not support open input stream.");
    }

    @Override
    public Observable<Integer> getWallpaperCount() {
        throw new UnsupportedOperationException("Cache data store not support get count.");
    }

    @Override
    public Observable<Void> refreshWallpapers() {
        return Observable.create(emitter -> {
            if (!NetworkUtil.isThereInternetConnection(context)) {
                emitter.onError(new NetworkConnectionException());
                emitter.onComplete();
                return;
            }
            android.accounts.Account account = Account.getAccount();
            String authority = context.getString(R.string.authority);
            ContentResolver
                    .setSyncAutomatically(account, authority, true);
            ContentResolver.setIsSyncable(account, authority, 1);

            boolean pending = ContentResolver.isSyncPending(account, authority);
            if (pending) {
                LogUtil.D(TAG, "Warning: sync is PENDING.");
            }
            boolean active = ContentResolver.isSyncActive(account, authority);
            if (active) {
                LogUtil.D(TAG, "Warning: sync is ACTIVE.");

            }

            if (pending || active) {
                LogUtil.D(TAG, "Has previously pending/active sync.");
                emitter.onError(new ResyncException());
                emitter.onComplete();
                return;
            }

            Bundle b = new Bundle();
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            b.putBoolean(SyncAdapter.SYNC_MANUALLY, true);
            LogUtil.D(TAG, "Requesting sync now.");

            ContentResolver.requestSync(account, authority, b);
            wallpaperCache.evictAll();
            emitter.onComplete();
        });
    }
}
