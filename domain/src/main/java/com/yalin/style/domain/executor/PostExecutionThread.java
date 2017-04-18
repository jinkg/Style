package com.yalin.style.domain.executor;

import io.reactivex.Scheduler;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */

public interface PostExecutionThread {
    Scheduler getScheduler();
}
