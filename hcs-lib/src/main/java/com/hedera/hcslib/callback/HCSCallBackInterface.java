package com.hedera.hcslib.callback;
/**
 * 
 * Callback interface so apps can register to receive messages from the library
 *
 */
public interface HCSCallBackInterface {  
    /**
     * method which will be called when a new message needs to be notified to the app
     * @param message the message to notify
     */
    void onMessage(String message);
}
