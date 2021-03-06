package com.yalin.style.data.lock;

import com.yalin.style.domain.executor.ThreadExecutor;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * YaLin
 * On 2017/4/30.
 */
@Singleton
public class OpenInputStreamLock extends ResourceLock {

    @Inject
    public OpenInputStreamLock(ThreadExecutor threadExecutor) {
        super(threadExecutor);
    }

    @Override
    protected Observable<Void> appendDelay(Observable<Void> observable) {
        return observable.delaySubscription(1, TimeUnit.SECONDS);
    }
}
