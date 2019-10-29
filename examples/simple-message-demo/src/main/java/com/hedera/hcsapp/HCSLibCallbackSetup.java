package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.callback.OnHCSMessageCallback;

/**
 * Hello world!
 *
 */
public final class HCSLibCallbackSetup 
{
    public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException
    {
        
        // example call back setup
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback();
        onHCSMessageCallback.addObserver(message -> {
            System.out.println("notified " + message);
        });
        onHCSMessageCallback.triggerCallBack();
    }

}
