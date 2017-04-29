package com.yalin.style.data.lock;

/**
 * YaLin
 * On 2017/4/30.
 */

public interface ResourceLock {

  boolean obtain();

  void release();
}
