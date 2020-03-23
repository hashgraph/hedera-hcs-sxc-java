package main.java.com.hedera.hcsapp;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.base.Joiner;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.VerifiableApplicationMessage;
import com.hedera.hcs.sxc.proto.VerifiedMessage;
import com.hedera.hcs.sxc.signing.Signing;
import com.hedera.hcs.sxc.utils.StringUtils;
import com.sun.source.util.TaskEvent;

import java.util.Map;

import lombok.extern.log4j.Log4j2;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
@Log4j2
public final class App2 {
    
    public static void main(String[] args) throws Exception {
        
        String appId = "";
        
        if (args.length == 0) {
            System.out.println("Missing application ID argument");
            System.exit(0);
        } else {
            appId = args[0];
        }
        
        int topicIndex = 0; // refers to the first topic ID in the config.yaml

        // 1 - Init the core with data from  .env and config.yaml 
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
                        hcsCore.addOrUpdateAppParticipant(k, v.get("theirEd25519PubKeyForSigning"), v.get("sharedSymmetricEncryptionKey"));
                    });
        }        


        System.out.println("****************************************");
        System.out.println("** Welcome to a simple HCS demo");
        System.out.println("** I am app: " + appId);
        System.out.println("** My signing key (private) is: " + hcsCore.getMessageSigningKey());
        System.out.println("** My signing key (public) is: " + hcsCore.getMessageSigningKey().publicKey);
        Map<String, Map<String, String>> addressList = AddressListCrypto.INSTANCE.getAddressList();
        System.out.println("** My buddies are: " + Joiner.on(",").withKeyValueSeparator("=").join(addressList));
        System.out.println("****************************************");
        
        
        
        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        onHCSMessageCallback.addObserver((HCSResponse hcsResponse) -> {
            try {
                
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<=============== Message received ====================================================");
                
                System.out.printf("Received from HCSResponse after consensus: %s \n",new String (hcsResponse.getMessage()));
                
                SxcApplicationMessageInterface applicationMessageEntity = 
                    hcsCore
                    .getPersistence()
                    .getApplicationMessageEntity(
                            SxcPersistence.extractApplicationMessageStringId(
                                    hcsResponse.getApplicationMessageId()
                            )
                    );
                System.out.println("Details stored as applicationMessageEntity : ");
                System.out.printf ("    applicationMessageId: %s \n",applicationMessageEntity.getApplicationMessageId());
                System.out.printf ("    last chrono chunk consensus sequenceNum: %s \n",applicationMessageEntity.getLastChronoPartSequenceNum());
                System.out.printf ("    last chrono chunk consensus running hash: %s \n",applicationMessageEntity.getLastChronoPartRunningHashHEX());
                System.out.println("    ApplicationMessage: ");
                ApplicationMessage appMessage = ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage());
                System.out.printf ("        Id: %s \n",SxcPersistence.extractApplicationMessageStringId(appMessage.getApplicationMessageId()));
                System.out.printf ("        Hash of unencrypted message: %s \n",
                    StringUtils.byteArrayToHexString(
                        appMessage.getUnencryptedBusinessProcessMessageHash().toByteArray()
                    )
                );
                System.out.printf ("        Signature on hash above: %s \n",
                    StringUtils.byteArrayToHexString(
                        appMessage.getBusinessProcessSignatureOnHash().toByteArray()
                    )
                );
                
                byte[] bpm = appMessage.getBusinessProcessMessage().toByteArray();
                
                try  { Any any = Any.parseFrom(bpm); 
                    if (any.is(VerifiableApplicationMessage.class)){
                        
                    } else if (any.is(VerifiedMessage.class)){
                        VerifiedMessage unpack = any.unpack(VerifiedMessage.class);
                                System.out.printf("        Message verification result: %s \n",
                                        unpack.getProved() 
                        ); 
                        
                    } else {
                        
                    }
                                         
                } catch (InvalidProtocolBufferException e){
                     // do nothing
                }
                
                        
                
                System.out.printf("        Encryption random: %s \n",
                    StringUtils.byteArrayToHexString(
                          appMessage.getEncryptionRandom().toByteArray()
                    )
                );
                
                System.out.printf("        Is this a self message?: %s \n",
                    Signing.verify(
                          appMessage.getUnencryptedBusinessProcessMessageHash().toByteArray()
                        , appMessage.getBusinessProcessSignatureOnHash().toByteArray()
                        , hcsCore.getMessageSigningKey().publicKey
                    )
                );
                
                
                
            } catch (InvalidProtocolBufferException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        Scanner scan = new Scanner(System.in);
        while (true) {
            
            // wait for user input
            System.out.println("- Input a message to send to other parties\n- type prove applicationId publickey (see above) [RETURN] to perform proof after the fact\n Send a random message first and then watch the output to get a usable application id. You can find a public key at the begining of this application. \n eg prove  0.0.95518-1584411224-26856500 302a300506032b657003210011ccdc3b4fa456254d9fec6abc015fa5512308d6a6e07d29754fadfd48f15227  \n- type exit [RETURN] to exit the application"
                    + ":");
            
            String userInput = scan.nextLine();
           
          
            
            
            if (userInput.isEmpty()) {
                System.out.println("Please input a message before pressing [RETURN].");
            } else if (userInput.equals("exit")) {
                scan.close();
                System.exit(0);
            } else if (userInput.startsWith("prove")) {
                String applicationMessageId = userInput.split("\\s+")[1];
                Ed25519PublicKey publicKey = Ed25519PublicKey.fromString(userInput.split("\\s+")[2]);
                    
                SxcApplicationMessageInterface applicationMessageEntity = hcsCore.getPersistence().getApplicationMessageEntity(applicationMessageId);
                if (applicationMessageEntity == null) {
                    System.out.println("Message not available in local db.");
                } else {
                    ByteString businessProcessMessage = ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage()).getBusinessProcessMessage();
                    try {
                        new OutboundHCSMessage(hcsCore)
                                .requestProof(0, applicationMessageEntity.getApplicationMessageId(),  businessProcessMessage.toStringUtf8(), publicKey);
                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============== Message sent to ALL participants (in addressbook) =============");
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
               
            } else {
                try {
                    new OutboundHCSMessage(hcsCore)
                        .sendMessage(topicIndex, userInput.getBytes());
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============== Message sent to ALL participants (in addressbook) =============");
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }            
    }
    
}
       
