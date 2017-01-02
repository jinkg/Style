package com.yalin.style.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.yalin.style.BuildConfig;

/**
 * YaLin On 2017/1/2.
 */

public class SettingsUtil {

  public static boolean isBootstrapDone(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getBoolean(BuildConfig.PREF_BOOTSTRAP_DONE, false);
  }

  public static void markBootstrapDone(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    sp.edit().putBoolean(BuildConfig.PREF_BOOTSTRAP_DONE, true).apply();
  }
}
