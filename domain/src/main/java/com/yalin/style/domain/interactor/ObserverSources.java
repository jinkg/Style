package com.yalin.style.domain.interactor;

import com.yalin.style.domain.observable.SourcesObservable;

import javax.inject.Inject;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public class ObserverSources {

    private SourcesObservable sourceObservable;

    @Inject
    public ObserverSources(SourcesObservable sourceObservable) {
        this.sourceObservable = sourceObservable;
    }

    public void registerObserver(DefaultObserver<Void> observer) {
        sourceObservable.registerObserver(observer);
    }

    public void unregisterObserver(DefaultObserver<Void> observer) {
        sourceObservable.unregisterObserver(observer);
    }

}
