package com.yalin.style.data.lock;

import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.interactor.DefaultObserver;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * YaLin
 * On 2017/4/30.
 */

public abstract class ResourceLock {
    private AtomicBoolean keeping = new AtomicBoolean(false);

    private final ThreadExecutor threadExecutor;

    private DefaultObserver<Void> releaseObserver;

    public ResourceLock(ThreadExecutor threadExecutor) {
        this.threadExecutor = threadExecutor;
    }

    public synchronized boolean obtain() {
        if (keeping.get()) {
            return false;
        } else {
            keeping.set(true);
            return true;
        }
    }

    public synchronized void release() {
        if (releaseObserver != null) {
            releaseObserver.dispose();
        }
        releaseObserver = new DefaultObserver<>();
        Observable<Void> observable = Observable.create(e -> {
            keeping.set(false);
            e.onComplete();
        });
        observable = appendDelay(observable);
        observable.subscribeOn(Schedulers.from(threadExecutor))
                .subscribeWith(releaseObserver);
    }

    protected Observable<Void> appendDelay(Observable<Void> observable) {
        return observable;
    }
}
