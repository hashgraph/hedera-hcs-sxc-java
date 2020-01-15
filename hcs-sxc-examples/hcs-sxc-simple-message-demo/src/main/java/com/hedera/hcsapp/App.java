package com.hedera.hcsapp;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;

import lombok.extern.log4j.Log4j2;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
@Log4j2
public final class App {
    
    public static void main(String[] args) throws Exception {
        
        long appId = 0;
        
        if (args.length == 0) {
            System.out.println("Missing application ID argument");
            System.exit(0);
        } else {
            appId = Long.parseLong(args[0]);
        }
        int topicIndex = 0; // refers to the first topic ID in the config.yaml
        
        // Simplest setup and send
        Config config = new Config();
        HCSCore hcsCore = new HCSCore(appId);

        System.out.println("****************************************");
        System.out.println("** Welcome to a simple HCS demo");
        System.out.println("** I am app: " + config.getConfig().getAppClients().get((int) appId).getClientName());
        System.out.println("****************************************");
        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        onHCSMessageCallback.addObserver(hcsResponse -> {
            System.out.println("Received : "+ hcsResponse.getMessage());
        });

        Scanner scan = new Scanner(System.in);
        while (true) {
            
            // wait for user input
            System.out.println("Input a message to send to other parties, type exit [RETURN] to exit the application"
                    + ":");
            String userInput = scan.nextLine();

           
            if (userInput.equals("exit")) {
                scan.close();
                System.exit(0);
            }
            
            if (userInput.isEmpty()) {
                System.out.println("Please input a message before pressing [RETURN].");
            } else {
                try {
                    new OutboundHCSMessage(hcsCore)
                        .overrideEncryptedMessages(false)
                        .overrideMessageSignature(false)
                        .sendMessage(topicIndex, userInput.getBytes());
    
                    System.out.println("Message sent successfully.");
                } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
                    log.error(e);
                }
            }
        }            
    }
}
       
