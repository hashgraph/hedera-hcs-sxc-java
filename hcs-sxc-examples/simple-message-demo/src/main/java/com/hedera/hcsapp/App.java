package com.hedera.hcsapp;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import com.hedera.hcslib.consensus.OutboundHCSMessage;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public final class App {
    
    public static void main(String[] args) throws Exception {
        
        int topicIndex = 0; // refers to the first topic ID in the config.yaml
        
        // Simplest setup and send
        Config config = new Config();
        Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();

        int appId = Integer.parseInt(dotEnv.get("APPID"));

        HCSLib hcsLib = new HCSLib(appId);

        System.out.println("****************************************");
        System.out.println("** Welcome to a simple HCS demo");
        System.out.println("** I am app: " + config.getConfig().getAppClients().get(appId).getClientName());
        System.out.println("****************************************");
        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsLib);
        onHCSMessageCallback.addObserver(message -> {
            System.out.println("Received : "+ message);
        });

        while (true) {
            
            // wait for user input
            Scanner scan = new Scanner(System.in);

            System.out.println("Input a message to send to other parties:");
            String userInput = scan.nextLine();
            
            if (userInput.equals("exit")) {
                System.exit(0);
            }
            
            Boolean messageSuccess;
            try {
                messageSuccess = new OutboundHCSMessage(hcsLib)
                        .overrideEncryptedMessages(false)
                        .overrideMessageSignature(false)
                        .sendMessage(topicIndex, userInput);

                if (messageSuccess) {
                    System.out.println("Message sent successfully.");
                }
            } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
                e.printStackTrace();
            }
        }            
    }
}
       