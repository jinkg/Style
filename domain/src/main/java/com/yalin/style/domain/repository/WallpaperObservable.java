package com.yalin.style.domain.repository;

import com.yalin.style.domain.interactor.DefaultObserver;

/**
 * @author jinyalin
 * @since 2017/5/23.
 */

public interface WallpaperObservable {
    void registerObserver(DefaultObserver<Void> observer);

    void unregisterObserver(DefaultObserver<Void> observer);
}
