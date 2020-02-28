package com.hedera.hcsapp;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
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
        
        String appId = "";
        
        if (args.length == 0) {
            System.out.println("Missing application ID argument");
            System.exit(0);
        } else {
            appId = args[0];
        }
        
        int topicIndex = 0; // refers to the first topic ID in the config.yaml

        // 2 - Init the core with data from  .env and config.yaml 
        //HCSCore hcsCore = HCSCore.INSTANCE.singletonInstance(appId);
        HCSCore hcsCore = new HCSCore().builder(appId,
                "./config/config.yaml",
                "./config/.env"+appId
        );
        
        if (hcsCore.getEncryptMessages()) {
            // 3 - Load the addressbook from address-list yaml and supply the core.
            AddressListCrypto
                    .INSTANCE
                    .singletonInstance(appId)
                    .getAddressList()
                    .forEach((k,v)->{
                        hcsCore.addAppParticipant(k, v.get("theirEd25519PubKeyForSigning"), v.get("sharedSymmetricEncryptionKey"));
                    });
        }        

        System.out.println("****************************************");
        System.out.println("** Welcome to a simple HCS demo");
        System.out.println("** I am app: " + appId);
        System.out.println("****************************************");
        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        onHCSMessageCallback.addObserver((HCSResponse hcsResponse) -> {
            System.out.println("Received : ");
            System.out.println(new String (hcsResponse.getMessage()));
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
                        //.overrideEncryptedMessages(false)
                        //.overrideMessageSignature(false)
                        .sendMessage(topicIndex, userInput.getBytes());
    
                    System.out.println("Message sent successfully.");
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }            
    }
}
       
