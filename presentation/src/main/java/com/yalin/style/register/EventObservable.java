package com.yalin.style.register;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.Observer;

/**
 * @author jinyalin
 * @since 2017/4/21.
 */
public class EventObservable<EVENT> {
    private Set<Observer<EVENT>> observers = new HashSet<>();

    public void subscribe(Observer<EVENT> register) {
        observers.add(register);
    }

    public void unsubscribe(Observer<EVENT> register) {
        observers.remove(register);
    }

    public void notify(EVENT event) {
        for (Observer<EVENT> observer : observers) {
            observer.onNext(event);
            observer.onComplete();
        }
    }
}
