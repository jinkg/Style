package com.yalin.style.data.repository.datasource.sync.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * YaLin 2017/1/3.
 */

public class AuthenticatorService extends Service {

  private Authenticator mAuthenticator;

  @Override
  public void onCreate() {
    super.onCreate();
    mAuthenticator = new Authenticator(this);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return mAuthenticator.getIBinder();
  }
}
