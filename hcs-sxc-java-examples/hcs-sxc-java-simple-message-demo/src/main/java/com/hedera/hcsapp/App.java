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
import com.google.common.base.Joiner;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ConfirmProof;
import com.hedera.hcs.sxc.signing.Signing;
import com.hedera.hcs.sxc.utils.StringUtils;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
@Log4j2
public final class App {
    private static String messageThread = ""; // used to group messages by an conversational id.
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

        //Init the core with data from  .env and config.yaml
        HCSCore hcsCore = new HCSCore().builder(appId,
                "./config/config.yaml",
                "./config/.env"+appId
        );

        if (hcsCore.getEncryptMessages()) {
            // Load the addressbook from address-list yaml "./config/contact-list.yaml"  and supply the core.
            AddressListCrypto addressBook = AddressListCrypto.INSTANCE.singletonInstance(appId);
            if (addressBook.getAddressList() != null) {
                // feed the core
                addressBook.supplyCore(hcsCore);
            }
        }


        Ansi.print("****************************************");
        Ansi.print("** Welcome to a simple HCS demo");
        Ansi.print("** I am app: " + appId);
        Ansi.print("** My private signing key is: " + hcsCore.getMessageSigningKey());
        Ansi.print("** My public signing key is: " + hcsCore.getMessageSigningKey().publicKey);
        Map<String, Map<String, String>> addressList = AddressListCrypto.INSTANCE.getAddressList();
        Ansi.print("** My buddies are: " + Joiner.on(",").withKeyValueSeparator("=").join(addressList));
        Ansi.print("****************************************");
        showHelp();

        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
        onHCSMessageCallback.addObserver((HCSResponse hcsResponse) -> {
            // handle notification in mirrorNotification
            mirrorNotification(hcsResponse, hcsCore);
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
                    // create a new thread  (sends message to HCS)
                    createNewThread(hcsCore, userInput);
                } else if (userInput.toUpperCase().startsWith("SELECT")) {
                    // selects a thread for messages to be sent (does not send HCS message)
                    selectThread(userInput);
                } else if (messageThread.isEmpty()) {
                    Ansi.print("(red)Please create or set a thread first(reset)");
                } else if (userInput.startsWith("prove")) {
                    // make a message verification request to a particular participant
                    String[] split = userInput.split("\\s+");
                    if (split.length != 4) {
                        System.out.println("Invalid number of argumets");
                    } else {
                        String player = split[1]; // this participant will verify the message
                        String applicationMessageId = split[2]; // this is the message to be verified - it's pulled from local db
                        String pubkey = split[3]; // this is the public key whose private key signed the message
                        try {
                            // find the message by ID in local database
                            Ed25519PublicKey publicKey = Ed25519PublicKey.fromString(pubkey);
                            SxcApplicationMessageInterface applicationMessageEntity = hcsCore.getPersistence().getApplicationMessageEntity(applicationMessageId);
                            if (applicationMessageEntity == null) {
                                System.out.println("Message not available in local db.");
                            } else {
                                // send the proof request to the network.
                                ByteString businessProcessMessage = ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage()).getBusinessProcessMessage();
                                try {
                                    new OutboundHCSMessage(hcsCore)
                                            .restrictTo(player)
                                            .requestProof(0, applicationMessageEntity.getApplicationMessageId(),  businessProcessMessage.toStringUtf8(), publicKey);
                                    System.out.println("proof request sent to "+player+"   awaiting  reply");
                                } catch (Exception e) {
                                    log.error(e);
                                }
                            }
                        } catch (Exception e){
                            System.out.println("Invalid public key");
                        }
                    }
                } else if (userInput.startsWith("send-restricted")) {
                    // send a message so that only one buddy from addressbook can decrypt
                    String[] split = userInput.split("\\s+");
                    if (split.length != 3) {
                        System.out.println("Invalid number of arguments, note: message cannot contain spaces");
                    } else {
                        sendMessageOnThread(hcsCore, messageThread, split[2], split[1]);
                    }
                } else {
                    // send a message so that all buddies from addresbook can decrypt
                    // send message on thread
                    sendMessageOnThread(hcsCore, messageThread, userInput, null);
                }
            }
        }
    }

    private static  void mirrorNotification(HCSResponse hcsResponse, HCSCore hcsCore) {
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

                printVerboseDetails(hcsCore,hcsResponse);
            } else if (simpleMessage.hasNewMessageThread()) {
                // creating a new thread
                String newThreadName = simpleMessage.getNewMessageThread().getThreadName();
                messageThreads.put(newThreadName, new ArrayList<String>());
                Ansi.print("(green)received thread creation notification from mirror: "
                        + "(yellow)" + newThreadName
                        + "(reset)");
                printVerboseDetails(hcsCore,hcsResponse);


            }
        } catch (InvalidProtocolBufferException e) {
            // request proof in progress
            System.out.println("        Echo of  proof request received. Awaiting replies. ");
        }
    }

    private static void printVerboseDetails (HCSCore hcsCore, HCSResponse hcsResponse){
        try  {
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
                if (any.is(ConfirmProof.class)){
                    ConfirmProof cf = any.unpack(ConfirmProof.class);

                    cf.getProofList().forEach(verifiedMessage ->{
                        System.out.printf("        Message verification result: %s \n",
                                verifiedMessage.getVerificationOutcome().name()
                        );
                    });
                }

            } catch (InvalidProtocolBufferException e){
                System.out.println("why here");
            }

            System.out.printf("        Encryption random: %s \n",
                    StringUtils.byteArrayToHexString(
                            appMessage.getEncryptionRandom().toByteArray()
                    )
            );

            System.out.printf("        Is this an echo?: %s \n",
                    Signing.verify(
                            appMessage.getUnencryptedBusinessProcessMessageHash().toByteArray()
                            , appMessage.getBusinessProcessSignatureOnHash().toByteArray()
                            , hcsCore.getMessageSigningKey().publicKey
                    )
            );

        } catch (InvalidProtocolBufferException ex){
             log.error(ex.getStackTrace());
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

    private static void sendMessageOnThread(HCSCore hcsCore, String threadName, String message, String playerId) {
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
            OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(hcsCore);
            if (playerId != null) outboundHCSMessage = outboundHCSMessage.restrictTo(playerId);
            outboundHCSMessage.sendMessage(topicIndex, simpleMessage.toByteArray());

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
        Ansi.print("(cyan)send-restricted (yellow) app_id (green) message (reset) to encrypt the message only with the paired appId's symetric key");
        Ansi.print("(cyan)prove (yellow) app_id (purple) applicationMessageId (green) public_key (reset) to request from app_id to prove message a after the fact ; you can generate a message first and copy its resulting application message id. ");
        Ansi.print("(cyan)help(reset) to print this help");
        Ansi.print("(cyan)exit(reset) to quit");
    }
}
