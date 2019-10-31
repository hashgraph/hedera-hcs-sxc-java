package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.callback.OnHCSMessageCallback;

import lombok.extern.log4j.Log4j2;

/**
 * Hello world!
 *
 */
@Log4j2
public final class HCSLibCallbackSetup 
{
    public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException
    {
        
        try {
            // TODO pass the config in teh custructor
            OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(null);
            onHCSMessageCallback.addObserver(
                    message -> {
                        
                        log.info("notified " + message);
                    }
            );
            //onHCSMessageCallback.;
        } catch (Exception ex) {
            log.error(ex);
        }
    }

}
