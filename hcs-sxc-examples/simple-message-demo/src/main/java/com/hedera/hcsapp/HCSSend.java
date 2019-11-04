package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import com.hedera.hcslib.consensus.OutboundHCSMessage;
import java.time.LocalDateTime;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Hello world!
 *
 */
public final class HCSSend {
    
       public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException, Exception
       {
      // Simplest setup and send
        HCSLib hcsLib = new HCSLib();
        
        
        try {
            
            // example of a single part message been sent
            
            Boolean singlePartSuccess = 
                    new OutboundHCSMessage(hcsLib)
                   .overrideEncryptedMessages(false)
                   .overrideMessageSignature(false)
                   .sendMessage(0, "HCSSend - "+LocalDateTime.now().toString());
            if (singlePartSuccess) {
                System.out.println("Single part message sent to HH network");
            }
            
            // example of a multi part message 
            String random5kString = RandomStringUtils.random(5000, true, true);
        
            Boolean multiPartSuccess = 
                    new OutboundHCSMessage(hcsLib)
                   .overrideEncryptedMessages(false)
                   .overrideMessageSignature(false)
                   .sendMessage(0, "HCSSend multi part - "+random5kString);
            if (multiPartSuccess) {
                System.out.println("Multi part message sent to HH network");
            }
            
            // create a callback obect to receive the message
            OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsLib);
            onHCSMessageCallback.addObserver(message -> {
                System.out.println("Observer received : "+ message);
            });
            
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 

}
       
