package com.voitov.ownobservableimpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of observer pattern.
 * Author: Vladislav Voitov, dobrihlopez@gmail.com
 */

public abstract class BaseObservable<LISTENER_CLASS> {
    private final Object MONITOR = new Object();
    private final Set<LISTENER_CLASS> listeners = new HashSet<>();

    public void registerListener(LISTENER_CLASS newListener) {
        synchronized (MONITOR) {
            boolean hadNoListeners = listeners.size() == 0;
            listeners.add(newListener);

            if (listeners.size() == 1 && hadNoListeners) {
                onFirstListenerRegistered();
            }
        }
    }

    public void unregisterListener(LISTENER_CLASS outOfUseListener) {
        synchronized (MONITOR) {
            boolean hadOnlyOneListener = listeners.size() == 1;
            listeners.remove(outOfUseListener);

            if (listeners.size() == 0 && hadOnlyOneListener) {
                onLastListenerUnregistered();
            }
        }
    }

    protected void onFirstListenerRegistered() {

    }

    protected void onLastListenerUnregistered() {

    }

    public Set<LISTENER_CLASS> getListeners() {
        return Collections.unmodifiableSet(new HashSet<>(listeners));
    }
}
