package com.hedera.hcs.sxc.interfaces;

/**
 * 
 * Callback interface so apps can register to receive messages from the core component
 *
 */
public interface HCSCallBackToAppInterface  {  
    /**
     * Functional interface method which will be called when a new message 
     * needs to be notified to the app
     * @param hcsResponse the hcs message to notify
     */
    void onMessage(HCSResponse hcsResponse);
}
