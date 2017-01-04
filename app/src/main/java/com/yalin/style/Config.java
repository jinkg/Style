package com.yalin.style;

import java.util.concurrent.TimeUnit;

/**
 * YaLin 2017/1/3.
 */

public class Config {

  public static final int DEFAULT_DOWNLOAD_TIMEOUT = 120; // in seconds
  public static final int DEFAULT_READ_TIMEOUT = 30; // in seconds
  public static final int DEFAULT_CONNECT_TIMEOUT = 15; // in seconds


  public static final long DEBUG_AUTO_SYNC_INTERVAL_LONG =
      TimeUnit.MILLISECONDS.convert(2L, TimeUnit.MINUTES);

  public static final long AUTO_SYNC_INTERVAL_LONG =
      TimeUnit.MILLISECONDS.convert(24L, TimeUnit.HOURS);
}
