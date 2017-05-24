package com.yalin.style.domain.observable;

import com.yalin.style.domain.interactor.DefaultObserver;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public interface SourcesObservable {
    void registerObserver(DefaultObserver<Void> observer);

    void unregisterObserver(DefaultObserver<Void> observer);
}
