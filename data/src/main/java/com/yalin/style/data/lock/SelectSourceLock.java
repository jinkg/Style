package com.yalin.style.data.lock;

import com.yalin.style.domain.executor.ThreadExecutor;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * YaLin
 * On 2017/4/30.
 */
@Singleton
public class SelectSourceLock extends ResourceLock {

    @Inject
    public SelectSourceLock(ThreadExecutor threadExecutor) {
        super(threadExecutor);
    }

    @Override
    protected Observable<Void> appendDelay(Observable<Void> observable) {
        return observable.delaySubscription(1, TimeUnit.SECONDS);
    }
}
