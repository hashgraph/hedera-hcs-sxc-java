package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public final class HCSLibCallbackSetup 
{
    public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException
    {
        
        try {
            // TODO pass the config in teh custructor
            OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(null);
            onHCSMessageCallback.addObserver(
                    message -> {
                        
                        System.out.println("notified " + message);
                    }
            );
            //onHCSMessageCallback.;
        } catch (Exception ex) {
            Logger.getLogger(HCSLibCallbackSetup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
