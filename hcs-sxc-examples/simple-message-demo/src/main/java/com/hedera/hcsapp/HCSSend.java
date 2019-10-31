package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;

/**
 * Hello world!
 *
 */
public final class HCSSend {
    
       public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException, Exception
       {
      // Simplest setup and send
        HCSLib hcsLib = new HCSLib();
        
        // Outbound message (app->lib->hedera example)
        try {
            Boolean success = new com.hedera.hcslib.consensus.OutboundHCSMessage(hcsLib)
                .sendMessage(0, "test");
            if (success) {
                System.out.println("Message sent to HH network");
            }
            
            // create a callback obect to receivet the message
            OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(null);
            onHCSMessageCallback.addObserver(message -> {
                
            });
            
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
        
//    public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException
//    {
//        
//        // Simplest setup and send
//        HCSLib hcsLib = new HCSLib();
//        
//        // Outbound message (app->lib->hedera example)
//        try {
//            Boolean success = new com.hedera.hcslib.consensus.OutboundHCSMessage(hcsLib)
//                .sendMessage(0, "test");
//            if (success) {
//                System.out.println("Message sent");
//            }
//        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
//        
//        // More complex HCSLib setup example
////        HCSLib hcsLib2 = new HCSLib()
////            .withEncryptedMessages(true)
////            .withKeyRotation(true, 10)
////            .withMessageSignature(true);
////        
////        // More complex outbound message (app->lib->hedera example) with overrides
////        try {
////            Boolean success = new com.hedera.hcslib.consensus.OutboundHCSMessage(hcsLib2)
////                .overrideEncryptedMessages(true)
////                .overrideKeyRotation(true, 10)
////                .overrideMessageSignature(false)
////                .sendMessage(0, "test");
////        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
////            // TODO Auto-generated catch block
////            e.printStackTrace();
////        }
//    }


