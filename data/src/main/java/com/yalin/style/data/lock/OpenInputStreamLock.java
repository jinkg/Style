package com.yalin.style.data.lock;

import com.yalin.style.domain.executor.ThreadExecutor;
import com.yalin.style.domain.interactor.DefaultObserver;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * YaLin
 * On 2017/4/30.
 */
@Singleton
public class OpenInputStreamLock implements ResourceLock {

  private AtomicBoolean inputStreamOpening = new AtomicBoolean(false);

  private final ThreadExecutor threadExecutor;

  private DefaultObserver<Void> releaseObserver;

  @Inject
  public OpenInputStreamLock(ThreadExecutor threadExecutor) {
    this.threadExecutor = threadExecutor;
  }

  @Override
  public synchronized boolean obtain() {
    if (inputStreamOpening.get()) {
      return false;
    } else {
      inputStreamOpening.set(true);
      return true;
    }
  }

  @Override
  public synchronized void release() {
    if (releaseObserver != null) {
      releaseObserver.dispose();
    }
    releaseObserver = new DefaultObserver<>();
    Observable<Void> observable = Observable.create(e -> {
      inputStreamOpening.set(false);
      e.onComplete();
    });
    observable.delaySubscription(1, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.from(threadExecutor))
        .subscribeWith(releaseObserver);
  }
}
