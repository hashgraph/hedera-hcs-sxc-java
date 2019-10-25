package com.hedera.hcslib.callback;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Implements callback registration and notification capabilities to support apps
 *
 */
public final class OnHCSMessageCallback {
    private final List<HCSCallBackInterface> observers = new ArrayList<>();

    /**
     * Adds an observer to the list of observers
     * @param listener
     */
    public void addObserver(HCSCallBackInterface listener) {
        observers.add(listener);
    }
    /**
     * Notifies all observers with the supplied message
     * @param message
     */
    void notifyObservers(String message){
        observers.forEach(listener -> listener.onMessage(message));
    }
    
  
    
    /**
     * For test purposes for now
     */
    //TODO: Remove this
    public void triggerCallBack() {
        notifyObservers("hi there");
    }
    
}
