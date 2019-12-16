package com.hedera.hcslib.interfaces;

/**
 * 
 * Callback interface so apps can register to receive messages from the library
 *
 */
public interface HCSCallBackInterface  {  
    /**
     * Functional interface method which will be called when a new message 
     * needs to be notified to the app
     * @param hcsResponse the hcs message to notify
     */
    void onMessage(HCSResponse hcsResponse);
}
