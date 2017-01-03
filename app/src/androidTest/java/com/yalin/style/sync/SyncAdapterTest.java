package com.yalin.style.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;
import com.yalin.style.provider.StyleContract;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * YaLin 2017/1/3.
 */
@RunWith(AndroidJUnit4.class)
public class SyncAdapterTest {

  @Test
  public void syncAdapterTest() {
//    ContentResolver
//        .setSyncAutomatically(null, StyleContract.AUTHORITY, true);
//    ContentResolver.setIsSyncable(account, ScheduleContract.CONTENT_AUTHORITY, 1);
    Account account = com.yalin.style.sync.account.Account.getAccount();
//    ContentResolver.setIsSyncable(account, StyleContract.AUTHORITY, 1);
    int syncable = ContentResolver.getIsSyncable(account, StyleContract.AUTHORITY);

    Bundle b = new Bundle();
    b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    ContentResolver.requestSync(account, StyleContract.AUTHORITY, b);
  }
}
