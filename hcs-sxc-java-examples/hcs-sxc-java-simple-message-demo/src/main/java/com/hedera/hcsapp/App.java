package com.hedera.hcsapp;

import com.google.protobuf.InvalidProtocolBufferException;
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
import proto.MessageOnThread;
import proto.MessageThread;
import proto.SimpleMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
@Log4j2
public final class App {
    // eclipse plugin to render ANSI colors in console if needed: mihai-nita.net/2013/06/03/eclipse-plugin-ansi-in-console 
    private static String messageThread = "";
    private static Map<String, List<String>> messageThreads = new HashMap<String, List<String>>();
    private static int topicIndex = 0; // refers to the first topic ID in the config.yaml

    public static void main(String[] args) throws Exception {

        String appId = "";
        
        if (args.length == 0) {
            Ansi.print("Missing application ID argument");
            System.exit(0);
        } else {
            appId = args[0];
        }

        // 1 - Init the core with data from  .env and config.yaml 
        //HCSCore hcsCore = HCSCore.INSTANCE.singletonInstance(appId);
        HCSCore hcsCore = new HCSCore().builder(appId,
                "./config/config.yaml",
                "./config/.env"+appId
        );

        if (hcsCore.getEncryptMessages()) {
            // 3 - Load the addressbook from address-list yaml and supply the core.
            if (AddressListCrypto.INSTANCE.singletonInstance(appId).getAddressList() != null) {
                AddressListCrypto
                        .INSTANCE
                        .singletonInstance(appId)
                        .getAddressList()
                        .forEach((k,v)->{
                            hcsCore.addOrUpdateAppParticipant(k, v.get("theirEd25519PubKeyForSigning"), v.get("sharedSymmetricEncryptionKey"));
                        });
            }
        }        

        Ansi.print("****************************************");
        Ansi.print("** Welcome to a simple HCS demo");
        Ansi.print("** I am app: " + appId);
        Ansi.print("****************************************");
        showHelp();
        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        onHCSMessageCallback.addObserver((HCSResponse hcsResponse) -> {
            // handle notification in mirrorNotification
            mirrorNotification(hcsResponse);
        });

        // wait for user input
        Scanner scan = new Scanner(System.in);
        while (true) {
            Ansi.print("");
            Ansi.print("(white)>(reset)");
            String userInput = scan.nextLine();

            switch (userInput.toUpperCase()) {
            case "EXIT":
                scan.close();
                Ansi.print("(purple)Goodbye.(reset)");
                System.exit(0);
            case "HELP":
                showHelp();
                break;
            case "LIST":
                //list all threads
                showThreadList();
                break;
            case "SHOW":
                if (messageThread.isEmpty()) {
                    Ansi.print("(red)Please create or select a thread first(reset)");
                } else {
                    // show all messages in a thread
                    showThreadMessages();
                }
                break;
            default:
                if (userInput.isEmpty()) {
                    Ansi.print("(red)Please input a message before pressing [RETURN].(reset)");
                } else if (userInput.toUpperCase().startsWith("NEW")) {
                    // create a new thread 
                    createNewThread(hcsCore, userInput);
                } else if (userInput.toUpperCase().startsWith("SELECT")) {
                    // selects a thread for messages to be sent
                    selectThread(userInput);
                } else if (messageThread.isEmpty()) {
                    Ansi.print("(red)Please create or set a thread first(reset)");
                } else {
                    // send message on thread
                    sendMessageOnThread(hcsCore, messageThread, userInput);
                }
            }
        }            
    }
    
    private static void mirrorNotification(HCSResponse hcsResponse) {
        // we receive a notification from mirror via HCS core
        try {
            // try to parse the notification into a proto
            SimpleMessage simpleMessage = SimpleMessage.parseFrom(hcsResponse.getMessage());
            // check the incoming protobuf message for instructions
            if (simpleMessage.hasMessageOnThread()) {
                // we have received a new message
                String message = simpleMessage.getMessageOnThread().getMessage();
                String threadName = simpleMessage.getMessageOnThread().getThreadName();
                List<String> messages = messageThreads.get(threadName);
                messages.add(message);
                messageThreads.put(threadName, messages);
                
                Ansi.print("(green)received new message notification from mirror on thread " 
                        + "(yellow)" + threadName 
                        + "(reset), message: " 
                        + "(yellow)" + message 
                        + "(reset)");
            } else if (simpleMessage.hasNewMessageThread()) {
                // creating a new thread
                String newThreadName = simpleMessage.getNewMessageThread().getThreadName();
                messageThreads.put(newThreadName, new ArrayList<String>());
                Ansi.print("(green)received thread creation notification from mirror: " 
                        + "(yellow)" + newThreadName 
                        + "(reset)");
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
    
    private static void showThreadList() {
        Ansi.print("(cyan)Known threads(reset)");
        for (Entry<String, List<String>> thread : messageThreads.entrySet()) {
            System.out.print("  ");
            if (thread.getKey().equals(messageThread)) {
                Ansi.print("(bold)" + thread.getKey() + "(reset)");
            } else {
                Ansi.print(thread.getKey());
            }
        }
    }
    
    private static void showThreadMessages() {
        Ansi.print("(cyan)Messages in thread " + messageThread + "(reset)");
        for (String message : messageThreads.get(messageThread)) {
            System.out.print("  ");
            Ansi.print(message);
        }
    }
    
    private static void sendMessageOnThread(HCSCore hcsCore, String threadName, String message) {
        Ansi.print("Sending... please wait.");
        // create a protobuf message to carry the message
        // note, we don't add the message to the thread until we receive notification from mirror
        // this helps keep consistent state between apps in an appnet
        MessageOnThread messageOnThread = MessageOnThread.newBuilder()
                .setThreadName(threadName)
                .setMessage(message)
                .build();
        
        // wrap the above in a simpleMessage proto
        SimpleMessage simpleMessage = SimpleMessage.newBuilder()
                .setMessageOnThread(messageOnThread)
                .build();
        
        try {
            // Send to HCS
            new OutboundHCSMessage(hcsCore)
                .sendMessage(topicIndex, simpleMessage.toByteArray());

            Ansi.print("Message sent to HCS successfully.");
        } catch (Exception e) {
            log.error(e);
        }
    }

    private static void createNewThread(HCSCore hcsCore, String input) {
        // remove `THREAD NEW` from the userInput to get the thread name
        try {
            String threadName = input.substring("new ".length()).trim();
            if (messageThreads.containsKey(threadName)) {
                Ansi.print("(red)Thread already exists(reset)");
            } else {
                Ansi.print("Sending... please wait.");
                // create a protobuf message to carry the new thread
                // note, we don't add the new thread to the threadlist until we receive notification from mirror
                // this helps keep consistent state between apps in an appnet
                MessageThread messageThread = MessageThread.newBuilder()
                        .setThreadName(threadName)
                        .build();
                
                // wrap the above in a simpleMessage proto
                SimpleMessage simpleMessage = SimpleMessage.newBuilder()
                        .setNewMessageThread(messageThread)
                        .build();
                try {
                    // Send to HCS
                    new OutboundHCSMessage(hcsCore)
                        .sendMessage(topicIndex, simpleMessage.toByteArray());
                    Ansi.print("Thread create message sent to HCS");
                } catch (Exception e) {
                    log.error(e);
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            Ansi.print("(red)Invalid input(reset)");
        }
    }
    
    private static void selectThread(String input) {
        // remove `THREAD SELECT` from the userInput to get the thread name
        try {
            String threadName = input.substring("select ".length()).trim();
            if (! messageThreads.containsKey(threadName)) {
                Ansi.print("(red)Unknown thread(reset)");
            } else {
                messageThread = threadName;
                Ansi.print(threadName + " is the current thread");
            }
        } catch (StringIndexOutOfBoundsException e) {
            Ansi.print("(red)Invalid input(reset)");
        }
    }
    
    private static void showHelp() {
        Ansi.print("Input these commands to interact with the application:");
        Ansi.print("(cyan)new (yellow)thread_name(reset) to create a new thread (purple)(note doesn't change current thread)(reset)");
        Ansi.print("(cyan)select (yellow)thread_name(reset) to create a new thread (purple)(note doesn't change current thread)(reset)");
        Ansi.print("(cyan)list(reset) to show a list of threads");
        Ansi.print("(cyan)show(reset) to list all messages for the current thread");
        Ansi.print("(cyan)help(reset) to print this help");
        Ansi.print("(cyan)exit(reset) to quit");
    }
}
       
