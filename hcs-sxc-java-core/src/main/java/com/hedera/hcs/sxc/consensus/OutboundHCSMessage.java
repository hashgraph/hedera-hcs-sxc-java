package com.hedera.hcs.sxc.consensus;

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

import com.google.common.hash.Hashing;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.config.Topic;
import com.hedera.hcs.sxc.interfaces.SxcKeyRotation;
import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.plugins.Plugins;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;
import com.hedera.hcs.sxc.proto.KeyRotationInitialise;
import com.hedera.hcs.sxc.proto.Timestamp;
import com.hedera.hcs.sxc.signing.Signing;
import com.hedera.hcs.sxc.utils.StringUtils;
import java.time.Instant;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyAgreement;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

@Log4j2
public final class OutboundHCSMessage {

    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private byte[] overrideMessageEncryptionKey = null; 
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
    private byte[] messageEncryptionKey = null;
    private Map<AccountId, String> nodeMap = new HashMap<>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0);
    private Ed25519PrivateKey ed25519PrivateKey;
    private long hcsTransactionFee = 0L;
    private List<Topic> topics;
    private TransactionId transactionId = null;
    private SxcPersistence persistencePlugin;
    private SxcMessageEncryption messageEncryptionPlugin;
    private SxcKeyRotation keyRotationPlugin;
    private Map<String,Map<String,String>> addressList = null;
    private HCSCore hcsCore;

    public OutboundHCSMessage(HCSCore hcsCore) throws Exception {
        this.hcsCore = hcsCore;
        this.signMessages = hcsCore.getSignMessages();
        this.encryptMessages = hcsCore.getEncryptMessages();
        this.rotateKeys = hcsCore.getRotateKeys();
        this.nodeMap = hcsCore.getNodeMap();
        this.operatorAccountId = hcsCore.getOperatorAccountId();
        this.ed25519PrivateKey = hcsCore.getEd25519PrivateKey();
        this.topics = hcsCore.getTopics();
        this.hcsTransactionFee = hcsCore.getMaxTransactionFee();
        this.addressList = hcsCore.getPersistence().getAddressList();

        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.hcs.sxc.plugin.persistence.*", "com.hedera.hcs.sxc.interfaces.SxcPersistence", true);
        this.persistencePlugin = (SxcPersistence)persistenceClass.newInstance();
        
        
        if(this.encryptMessages){
            Class<?> messageEncryptionClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcMessageEncryption", true);
            this.messageEncryptionPlugin = (SxcMessageEncryption)messageEncryptionClass.newInstance();
        }
        
        if(this.rotateKeys){
            
            Class<?> messageKeyRotationClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcKeyRotation", true);
            this.keyRotationPlugin = (SxcKeyRotation)messageKeyRotationClass.newInstance();
        }
    }

    
    public boolean getOverrideMessageSignature() {
        return this.signMessages;
    }
    
    public OutboundHCSMessage overrideMessageSignature(boolean signMessages) {
        this.signMessages = signMessages;
        return this;
    }

    public boolean getOverrideEncryptedMessages() {
        return this.encryptMessages;
    }
 
    public OutboundHCSMessage overrideEncryptedMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
        return this;
    }

    public boolean getOverrideKeyRotation() {
        return this.rotateKeys;
    }
 
    public int getOverrideKeyRotationFrequency() {
        return this.rotationFrequency;
    }

    public OutboundHCSMessage overrideKeyRotation(boolean keyRotation, int frequency) {
        this.rotateKeys = keyRotation;
        this.rotationFrequency = frequency;
        return this;
    }

    public Map<AccountId, String> getOverrideNodeMap() {
        return this.nodeMap;
    }

    public OutboundHCSMessage overrideNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }

    public AccountId getOverrideOperatorAccountId() {
        return this.operatorAccountId;
    }

    public OutboundHCSMessage overrideOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }

    public Ed25519PrivateKey getOverrideOperatorKey() {
        return this.ed25519PrivateKey;
    }

    public OutboundHCSMessage overrideOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }
    
    public TransactionId getFirstTransactionId() {
        return this.transactionId;
    }

    
    public OutboundHCSMessage overrideMessageEncryptionKey (byte[] messageEncryptionKey){
        this.overrideMessageEncryptionKey = messageEncryptionKey;
        return this;
    }
    
    public OutboundHCSMessage withFirstTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
        return this;
    }
    
    public OutboundHCSMessage restrictTo(String... appIds){
        Map<String, Map<String, String>> newAddressList = this.addressList;
        for (String appId: appIds ){
            newAddressList.put(appId, this.addressList.get(appId));
        }
        this.addressList = newAddressList;
        return this;
    }

    /**
     * Sends a single cleartext message
     *
     * @param topicIndex the index reference in one of {@link #topics}
     * @param message
     * @throws HederaNetworkException
     * @throws IllegalArgumentException
     * @throws Exception
     * @return TransactionId
     */
    public List<TransactionId> sendMessage(int topicIndex, byte[] message) throws Exception {
        List<TransactionId> txIdList = new ArrayList<>();
        
        if (encryptMessages) { // send  to specific users 
            if (this.addressList != null){ // get it from .env
                for (String recipient : addressList.keySet()){
                    TransactionId doSendMessageTxId = doSendMessage(message, topicIndex, recipient);
                    txIdList.add(doSendMessageTxId);
                }                
                //   this.messageEncryptionKey = hcsCore.getMessageEncryptionKey();
                //   message = messageEncryptionPlugin.encrypt(this.messageEncryptionKey, message);
            } else {
                throw new NoSuchElementException("Encryption set to true, but keys not found in address book");
            }
        } else { // broadcast
            TransactionId doSendMessageTxId = doSendMessage(message, topicIndex, null);
            txIdList.add(doSendMessageTxId);     
        }
        
        
        return txIdList;
    }

    private TransactionId doSendMessage(byte[] message, int topicIndex, String recipient) {
        // generate TXId for main and first message if not already set by caller
        TransactionId firstTransactionId = (this.transactionId == null) ? new TransactionId(this.operatorAccountId) : this.transactionId;
        //break up  (and the whole encrypted messages
        List<ApplicationMessageChunk> parts = chunk(firstTransactionId, hcsCore, message, this.addressList.get(recipient));
        // send each part to the network
        try (Client client = new Client(this.nodeMap)) {
            
            if(this.operatorAccountId == null
                    ||
                    this.ed25519PrivateKey == null){
                System.out.println("Operator key or operator id not set. Exiting... ");
                System.exit(0);
            }
            client.setOperator(
                    this.operatorAccountId,
                    this.ed25519PrivateKey
            );

            client.setMaxTransactionFee(this.hcsTransactionFee);

            TransactionId transactionId = firstTransactionId;
            int count = 1;
            for (ApplicationMessageChunk messageChunk : parts) {
                log.debug("Sending message part " + count + " of " + parts.size() + " to topic " + this.topics.get(topicIndex).toString());
                count++;
                ConsensusMessageSubmitTransaction tx = new ConsensusMessageSubmitTransaction()
                        .setMessage(messageChunk.toByteArray())
                        .setTopicId(this.topics.get(topicIndex).getConsensusTopicId())
                        .setTransactionId(transactionId);
                
                if ((this.topics.get(topicIndex).getSubmitKey() != null) && (! this.topics.get(topicIndex).getSubmitKey().isEmpty())) {
                    // sign if we have a submit key
                    tx.build(client).sign(Ed25519PrivateKey.fromString(this.topics.get(topicIndex).getSubmitKey()));
                }

                // persist the transaction
                this.persistencePlugin.storeTransaction(transactionId, tx);

                log.debug("Executing transaction");
                TransactionId txId = tx.execute(client);
                
                TransactionReceipt receipt = txId.getReceipt(client, Duration.ofSeconds(30));

                transactionId = new TransactionId(this.operatorAccountId);

                log.debug("Message receipt status is {} "
                        + "sequence no is {}"
                        ,receipt.status
                        ,receipt.getConsensusTopicSequenceNumber()
                );
            } // end-for
            
            // after sending all parts check if key rotation is due
            if (rotateKeys) {
                if (!this.encryptMessages) {
                    throw new Exception("Trying to initiate key rotation but encryption is disabled");   
                }
                   
                int messageCount = -1; //TODO - keep track of messages pair-wise, not just here. ( per topic )
                if (messageCount < rotationFrequency) { // TODO - Fires everytime
                        
                    //1) Send initiate  Message so that his onHCSMessage can pick it up
                    //2) If onHCSMessage receives KR1 then update key and KR2
                    //3) If onHCSMessage receives KR2 then update key using stored KeyAgreement
                    
                    Pair<KeyAgreement, byte[]> initiate = keyRotationPlugin.initiate();
                    //store the KeyAgreement to HCSCore to refetch when finalising
                    hcsCore.setTempKeyAgreement(initiate.getLeft());
                    //prepare yielded PK and send to network
                    KeyRotationInitialise kr1 = KeyRotationInitialise.newBuilder()
                            .setInitiatorPublicKeyEncoded(ByteString.copyFrom(initiate.getRight()))
                            .build();
                    Any anyPack = Any.pack(kr1);
                    byte[] encryptedAnyPackedChunkBody = messageEncryptionPlugin.encrypt(this.messageEncryptionKey, anyPack.toByteArray());
                    
                    
                    TransactionId newTransactionId = new TransactionId(hcsCore.getOperatorAccountId());
                    ApplicationMessageID newAppId = ApplicationMessageID.newBuilder()
                            .setAccountID(AccountID.newBuilder()
                                    .setShardNum(newTransactionId.accountId.shard)
                                    .setRealmNum(newTransactionId.accountId.realm)
                                    .setAccountNum(newTransactionId.accountId.account)
                                    .build()
                            )
                            .setValidStart(Timestamp.newBuilder()
                                    .setSeconds(newTransactionId.validStart.getEpochSecond())
                                    .setNanos(newTransactionId.validStart.getNano())
                                    .build()
                            ).build();
                    
                    ApplicationMessageChunk appChunk = ApplicationMessageChunk.newBuilder()
                            .setApplicationMessageId(newAppId)
                            .setChunkIndex(1)
                            .setChunksCount(1)
                            .setMessageChunk(
                                    //fit an antire ApplicationMessage in the chunk and set body message to the encrypted stuff
                                    ApplicationMessage.newBuilder()
                                            .setApplicationMessageId(newAppId)
                                            //TODO: set signature
                                            .setBusinessProcessMessage(ByteString.copyFrom(encryptedAnyPackedChunkBody))
                                            //TODO: set hash
                                            .build()
                                            .toByteString()
                            ).build();  
                    
                    ConsensusMessageSubmitTransaction txRotation = new ConsensusMessageSubmitTransaction()
                            .setMessage(appChunk.toByteArray())
                            .setTopicId(this.topics.get(topicIndex).getConsensusTopicId())
                            .setTransactionId(newTransactionId);
                    
                    // persist the transaction
                    this.persistencePlugin.storeTransaction(newTransactionId, txRotation);
                    TransactionId txIdKR1 =  txRotation.execute(client);
                    
                    TransactionReceipt receiptKR1 = txIdKR1.getReceipt(client, Duration.ofSeconds(30));
                    
                    log.debug("Message receipt for KR1 status is {} "
                            + "sequence no is {}"
                            ,receiptKR1.status
                            ,receiptKR1.getConsensusTopicSequenceNumber()
                    );
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // do nothing
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        } finally {
            
        }
        return firstTransactionId;
    }

    public static  List<ApplicationMessageChunk> chunk(TransactionId transactionId, HCSCore hcsCore, byte[] message, Map<String,String> recipientKeys) {

        ApplicationMessageID transactionID = ApplicationMessageID.newBuilder()
                .setAccountID(AccountID.newBuilder()
                        .setShardNum(transactionId.accountId.shard)
                        .setRealmNum(transactionId.accountId.realm)
                        .setAccountNum(transactionId.accountId.account)
                        .build()
                )
                .setValidStart(Timestamp.newBuilder()
                        .setSeconds(transactionId.validStart.getEpochSecond())
                        .setNanos(transactionId.validStart.getNano())
                        .build()
                ).build();

        byte[] originalMessage = Arrays.copyOf(message, message.length);
        
        ApplicationMessage applicationMessage  = null;
        ApplicationMessage.Builder applicationMessageBuilder = ApplicationMessage
                .newBuilder()
                .setApplicationMessageId(transactionID);
                
        if(recipientKeys != null){
            try {
                // build one encrypted and one unecrypted message. Store the latter in the core db
                
                // Hash of unencrypted business message should be included in application message
                byte[] hashOfOriginalMessage = com.hedera.hcs.sxc.hashing.Hashing.sha(StringUtils.byteArrayToHexString(originalMessage));
                applicationMessageBuilder.setBusinessProcessHash(ByteString.copyFrom(hashOfOriginalMessage));
                
                // Signature (using sender’s private key) of hash (above) should also be included in application message
                Ed25519PrivateKey messageSigningKey = hcsCore.getMessageSigningKey();
                
                byte[] sign = Signing.sign(hashOfOriginalMessage, messageSigningKey);
                applicationMessageBuilder.setBusinessProcessSignature(ByteString.copyFrom(sign));
                
                // encrypt
                String encryptionKey = recipientKeys.get("sharedSymmetricEncryptionKey");
                Class<?> messageEncryptionClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcMessageEncryption", true);
                SxcMessageEncryption encPlugin = (SxcMessageEncryption)messageEncryptionClass.newInstance();
                //Any.pack(message);
                byte[] encryptedMessage = encPlugin.encrypt(
                        StringUtils.hexStringToByteArray(encryptionKey)
                        , message);
                applicationMessageBuilder.setBusinessProcessMessage(
                        ByteString.copyFrom(encryptedMessage)
                );
                
                
                
                applicationMessage = applicationMessageBuilder.build();
                
                // store the outgoing message unencrypted - null parameters because missing consensus data. 
                // Consensus state is sored on inbound messages
                // This one is needed to know if the message was sent by me
                // because I don't have a way to un-encrypt my own message
                // I wouldn't now know what encryption key I used
                
                applicationMessageBuilder.setBusinessProcessMessage(ByteString.copyFrom(message));
                ApplicationMessage tempUnencryptedAppMsg = applicationMessageBuilder.build();
                
                
                hcsCore.getPersistence().storeApplicationMessage(
                        //add recipient
                        tempUnencryptedAppMsg, 
                        null, 
                        null, 0
                );    
           
               
                  
            } catch (Exception ex) {
                Logger.getLogger(OutboundHCSMessage.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0);
            }
            
        } else {
            applicationMessageBuilder.setBusinessProcessMessage(ByteString.copyFrom(originalMessage));
            applicationMessage = applicationMessageBuilder.build();
        }
        
        
        
        
        
        
        List<ApplicationMessageChunk> parts = new ArrayList<>();

        //TransactionID transactionID = messageEnvelope.getMessageEnvelopeId();
        byte[] amByteArray = applicationMessage.toByteArray();
        final int amByteArrayLength = amByteArray.length;
        // break up byte array into 3500 bytes parts
        final int chunkSize = 3500; // the hcs tx limit is 4k - there are header bytes that will be added to that
        int totalParts = (int) Math.ceil((double) amByteArrayLength / chunkSize);
        // chunk and send to network
        for (int i = 0, partId = 1; i < amByteArrayLength; i += chunkSize, partId++) {

            byte[] amMessageChunk = Arrays.copyOfRange(
                    amByteArray,
                    i,
                    Math.min(amByteArrayLength, i + chunkSize)
            );

            ApplicationMessageChunk applicationMessageChunk = ApplicationMessageChunk.newBuilder()
                    .setApplicationMessageId(transactionID)
                    .setChunkIndex(partId)
                    .setChunksCount(totalParts)
                    .setMessageChunk(ByteString.copyFrom(amMessageChunk))
                    .build();

            parts.add(applicationMessageChunk);
        }
        return parts;
    }
}
