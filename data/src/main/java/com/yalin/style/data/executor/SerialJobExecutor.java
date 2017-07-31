package com.yalin.style.data.executor;

import android.support.annotation.NonNull;


import com.yalin.style.domain.executor.SerialThreadExecutor;

import java.util.ArrayDeque;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jinyalin
 * @since 2017/4/18.
 */
@Singleton
public class SerialJobExecutor extends JobExecutor implements SerialThreadExecutor {
    final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
    Runnable mActive;

    @Inject
    SerialJobExecutor() {
        super();
    }

    @Override
    public synchronized void execute(@NonNull Runnable command) {
        mTasks.offer(() -> {
            try {
                command.run();
            } finally {
                scheduleNext();
            }
        });
        if (mActive == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((mActive = mTasks.poll()) != null) {
            threadPoolExecutor.execute(mActive);
        }
    }
}
