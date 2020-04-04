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

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.EncryptedData;
import com.hedera.hcs.sxc.config.Topic;
import com.hedera.hcs.sxc.interfaces.SxcKeyRotation;
import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.plugins.Plugins;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;
import com.hedera.hcs.sxc.proto.RequestProof;
import com.hedera.hcs.sxc.proto.Timestamp;
import com.hedera.hcs.sxc.proto.VerifiableApplicationMessage;
import com.hedera.hcs.sxc.proto.VerifiableMessage;
import com.hedera.hcs.sxc.signing.Signing;
import com.hedera.hcs.sxc.utils.StringUtils;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.log4j.Log4j2;

/**
 * Object used to invoke outbound creation message methods. See 
 * constructor {@link #OutboundHCSMessage(com.hedera.hcs.sxc.HCSCore) } for details. 
 */
@Log4j2
public final class OutboundHCSMessage {

    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private String overrideMessageEncryptionKey = null; 
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
    //private byte[] messageEncryptionKey = null;
    private Map<AccountId, String> nodeMap = new HashMap<>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0);
    private Ed25519PrivateKey operatorKey;
    private long hcsTransactionFee = 0L;
    private List<Topic> topics;
    private TransactionId transactionId = null;
    private SxcPersistence persistencePlugin;
    private SxcMessageEncryption messageEncryptionPlugin;
    private SxcKeyRotation keyRotationPlugin;
    private Map<String,Map<String,String>> addressList = null;
    private HCSCore hcsCore;

    /**
     * Instantiates object to provide outbound message functionality to end-
     * users. The objects requires and instance of {@link HCSCore} which
     * provides the app with necessary configuration and initialisation
     * parameters. End-users invoke the object's
     * {@link #sendMessage(int, byte[])} to send the payload to the HCS network.
     * The payload can be of any type and is wrapped in an
     * {@link ApplicationMessage} and sent over to the network. The object also
     * allows to make message verification requests with {@link #requestProof(int, java.lang.String, java.lang.String, com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey)
     * }
     * which sends special messages that receiving parties automatically
     * recognize and handle appropriately.
     * <p>
     * Behind the scenes, a message is split and is sent in chunks if it is too big
     * for the network to handle.
     * <p>
     * A builder pattern is used to to parameterize how messages (either
     * standard of proof requests) are sent.
     * <p>
     * When a message is sent with encryption enabled (option held in
     * HCSCore) then the message is encrypted with each shared key of every
     * participant in the address-book, unless encryption is restricted with {@link #restrictTo(java.util.List) } to a
     * single participant or a list of participants. Receiving parties can get all
     * messages whether these are encrypted or in clear-text.
     *
     * @param hcsCore instantiated core object that hold initialisation parameters, address-book etc. 
     * @throws Exception
     */
    public OutboundHCSMessage(HCSCore hcsCore) throws Exception {
        this.hcsCore = hcsCore;
        this.signMessages = hcsCore.getSignMessages();
        this.encryptMessages = hcsCore.getEncryptMessages();
        this.rotateKeys = hcsCore.getRotateKeys();
        this.nodeMap = hcsCore.getNodeMap();
        this.operatorAccountId = hcsCore.getOperatorAccountId();
        this.operatorKey = hcsCore.getOperatorKey();
        this.topics = hcsCore.getTopics();
        this.hcsTransactionFee = hcsCore.getMaxTransactionFee();
        this.addressList = hcsCore.getPersistence().getAddressList();

        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.hcs.sxc.plugin.persistence.*", "com.hedera.hcs.sxc.interfaces.SxcPersistence", true);
        this.persistencePlugin = (SxcPersistence)persistenceClass.newInstance();
        
        
       
        
        if(this.rotateKeys){
            
            Class<?> messageKeyRotationClass = Plugins.find("com.hedera.hcs.sxc.plugin.encryption.*", "com.hedera.hcs.sxc.interfaces.SxcKeyRotation", true);
            this.keyRotationPlugin = (SxcKeyRotation)messageKeyRotationClass.newInstance();
        }
    }

    private void enableMessageEncryptionPlugin() throws Exception {
        Class<?> messageEncryptionClass = Plugins.find("com.hedera.hcs.sxc.plugin.encryption.*", "com.hedera.hcs.sxc.interfaces.SxcMessageEncryption", true);
        this.messageEncryptionPlugin = (SxcMessageEncryption)messageEncryptionClass.newInstance();
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
        return this.operatorKey;
    }

    public OutboundHCSMessage overrideOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.operatorKey = ed25519PrivateKey;
        return this;
    }
    
    public TransactionId getFirstTransactionId() {
        return this.transactionId;
    }

    
    public OutboundHCSMessage overrideMessageEncryptionKey (String messageEncryptionKey){
        this.overrideMessageEncryptionKey = messageEncryptionKey;
        return this;
    }
    
    public String getOverrideMessageEncryptionKey (){
        return this.overrideMessageEncryptionKey;
    }

    public OutboundHCSMessage withFirstTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
        return this;
    }
    
    public OutboundHCSMessage restrictTo(String... appIds){
       return restrictTo(List.of(appIds));
    }

    public OutboundHCSMessage restrictTo(List<String> appIds){
        // linked hashmap to preserve order
        Map<String, Map<String, String>> newAddressList = new LinkedHashMap<String, Map<String, String>>();
        for (String appId: appIds ){
            newAddressList.put(appId, this.addressList.get(appId));
        }
        this.addressList = newAddressList;
        return this;
    }
    
     /**
     * Sends a single cleartext message but doesn't send to HCS 
     * This is for testing purposes only
     *
     * @param topicIndex the index reference in one of {@link #topics}
     * @param message
     * @throws Exception
     * @return TransactionId
     */
    public List<TransactionId> sendMessageForTest(int topicIndex, byte[] message) throws Exception {
        return sendMessage(topicIndex, message, true);
    }

     /**
     * Sends a single message which is wrapped into the payload field of an {@link ApplicationMessage}
     * Behind the scenes, large messages are split into chunks and sent to HCS - receiving parties assemble chunks
     * transparently. 
     * The message is sent encrypted
     * if the flag {@link #encryptMessages} is set in HCSCore via configuration files or when 
     * the {@link #overrideEncryptedMessages(boolean) } flag is sat during the builder construction. 
     * When encryption is enabled then 
     * multiple messages are sent, one for each participant with which an encryption key is shared. 
     * To restrict encryption to a subset of address book participants use {@link #restrictTo(java.util.List) } 
     * in the builder pattern of this objects constructor. 
     *
     * @param topicIndex the index reference in one of {@link #topics}
     * @param message
     * @throws Exception
     * @return TransactionId
     */
     public List<TransactionId> sendMessage(int topicIndex, byte[] message) throws Exception {
        return sendMessage(topicIndex, message, false);
    }
     
    /**
     * Sends a proof request to the network. The request is handled automatically by app-net 
     * participants where their own {@link OnHCSMessageCallback} proof request 
     * handling procedure replies results back to this participant. This method 
     * is using the same builder pattern as  {@link #sendMessage(int, byte[]) } where
     * requests restricted to particular participants are issued by composing
     * {@link  #restrictTo(java.util.List) } into the request. 
     * @param topicIndex
     * @param applicationMessageId The application message to be validated needs to reside
     * in own database either in encrypted or decrypted form.
     * @param cleartext The decrypted cleartext or business process message
     * @param publicKey The key of the signer of the message being validated
     * @return The transaction id's associated with the request. Notice that the verification 
     * result is returned asynchronously and is handled in {@link OnHCSMessageCallback#prove(com.hedera.hcs.sxc.proto.VerifiableApplicationMessage) }
     * @throws Exception 
     */ 
    public List<TransactionId> requestProof(int topicIndex, String applicationMessageId, String cleartext, Ed25519PublicKey publicKey) throws Exception {
        
        RequestProof rp = RequestProof.newBuilder()
                .addApplicationMessage(
                        VerifiableMessage.newBuilder().
                                setVerifiableApplicationMessage(
                                        VerifiableApplicationMessage.newBuilder()
                                                .setApplicationMessageId(
                                                    SxcPersistence.getApplicationMessageIdIdFromPrimaryKey(applicationMessageId)
                                                )                      
                                        .setOriginalBusinessProcessMessage(ByteString.copyFrom(cleartext.getBytes()))
                                        .setSenderPublicSigningKey(ByteString.copyFrom(publicKey.toBytes()))
                                        .build()
                                ).build()
                ).build();
        Any pack = Any.pack(rp);
        return this.sendMessage(topicIndex,pack.toByteArray());
    }
     
     
    public List<TransactionId> sendMessage(int topicIndex, byte[] message, boolean byPassSending) throws Exception {
        List<TransactionId> txIdList = new ArrayList<>();
        if(encryptMessages && this.overrideMessageEncryptionKey!=null ){
                log.debug("Override encryption");
                TransactionId doSendMessageTxId = doSendMessage(message, topicIndex, this.overrideMessageEncryptionKey, byPassSending);
                txIdList.add(doSendMessageTxId);
        }else if (encryptMessages) { // send  so that specific users can decrypt - flag needs addressbook
            enableMessageEncryptionPlugin();
             if (this.addressList != null && this.addressList.size() > 0){ // get it from .env
                for (String recipient : addressList.keySet()){
                    log.debug("Sending to " + recipient);
                    TransactionId doSendMessageTxId = doSendMessage(message, topicIndex, this.addressList.get(recipient).get("sharedSymmetricEncryptionKey") , byPassSending);
                    txIdList.add(doSendMessageTxId);
                }                
            } else {
                throw new NoSuchElementException("Encryption set to true, but no keys found in address book");
            }
        } else { // broadcast
            TransactionId doSendMessageTxId = doSendMessage(message, topicIndex, null, byPassSending);
            txIdList.add(doSendMessageTxId);     
        }
        return txIdList;
    }

    private TransactionId doSendMessage(byte[] message, int topicIndex, String recipientSharedSymetricEncryptionKey, boolean byPassSending) throws Exception {
        // generate TXId for main and first message it not already set by caller
        TransactionId firstTransactionId = this.transactionId;
        if (firstTransactionId == null) {
            firstTransactionId = new TransactionId(this.operatorAccountId);
        }
       
        ApplicationMessage applicationMessage = OutboundHCSMessage.userMessageToApplicationMessage(
                firstTransactionId,
                message,
                hcsCore.getMessageSigningKey(),  // null means don't sign
                recipientSharedSymetricEncryptionKey == null
                    ? null // don't encrypt
                    : recipientSharedSymetricEncryptionKey // encrypt
        );  
        
        //break up 
        List<ApplicationMessageChunk> parts = chunk(applicationMessage);
        
        // store the outgoing message unencrypted - use null parameters because 
        // missing consensus data. 
        // (consensus state is sored on inbound messages)
        // This one is needed to know if the message was sent by me
        // because I don't have a way to un-encrypt my own message and
        // I wouldn't know what encryption key I used.

        ApplicationMessage tempUnencryptedAppMsg =  applicationMessage.toBuilder()
            .setBusinessProcessMessage(ByteString.copyFrom(message))
            .build();
        hcsCore.getPersistence().storeApplicationMessage(
                //add recipient
                tempUnencryptedAppMsg, 
                null, 
                null, 0
        );    

        
        // send each part to the network
        try (Client client = new Client(this.nodeMap)) {
            
            if(this.operatorAccountId == null
                    ||
                    this.operatorKey == null){
                log.error("Operator key or operator id not set. Exiting... ");
                System.exit(0);
            }
            client.setOperator(
                    this.operatorAccountId,
                    this.operatorKey
            );

            client.setMaxTransactionFee(this.hcsTransactionFee);

            TransactionId transactionIdPrime = firstTransactionId;
            int count = 1;
            for (ApplicationMessageChunk messageChunk : parts) {
                log.debug("Sending message part " + count + " of " + parts.size() + " to topic " + this.topics.get(topicIndex).toString());
                count++;
                ConsensusMessageSubmitTransaction tx = new ConsensusMessageSubmitTransaction()
                        .setMessage(messageChunk.toByteArray())
                        .setTopicId(this.topics.get(topicIndex).getConsensusTopicId())
                        .setTransactionId(transactionIdPrime);
                
                if ((this.topics.get(topicIndex).getSubmitKey() != null) && (! this.topics.get(topicIndex).getSubmitKey().isEmpty())) {
                    // sign if we have a submit key
                    tx.build(client).sign(Ed25519PrivateKey.fromString(this.topics.get(topicIndex).getSubmitKey()));
                }

                // persist the transaction
                this.persistencePlugin.storeTransaction(transactionIdPrime, tx);

                log.debug("Executing transaction");
                if ( ! byPassSending) {
                    TransactionId txId = tx.execute(client);
                    
                    TransactionReceipt receipt = txId.getReceipt(client, Duration.ofSeconds(30));
    
                    transactionIdPrime = new TransactionId(this.operatorAccountId);

                    log.debug("Message receipt status is {} "
                            + "sequence no is {}"
                            ,receipt.status
                            ,receipt.getConsensusTopicSequenceNumber()
                    );
                }
            } // end-for
            
            // after sending all parts check if key rotation is due
            if (rotateKeys) {
                
                /** ROTATION CODE - UNCOMMENT TO ENABLE
                
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
                        if ( ! byPassSending) {
                            TransactionId txIdKR1 =  txRotation.execute(client);
                            
                            TransactionReceipt receiptKR1 = txIdKR1.getReceipt(client, Duration.ofSeconds(30));
                           
                            log.debug("Message receipt for KR1 status is {} "
                                    + "sequence no is {}"
                                    ,receiptKR1.status
                                    ,receiptKR1.getConsensusTopicSequenceNumber()
                            );
                        } else {
                            log.warn("Not sending, bypassing sending for testing");
                        }
                    }
                ROTATION CODE - UNCOMMENT TO ENABLE */
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // do nothing
        } catch (Exception e) {
            log.error(e);
            throw (e);
        } finally {
            
        }
        return firstTransactionId;
    }
    
    
    /**
     * Wraps a user messages into an ApplicationMessage. 
     * @param message the user message
     * @param senderSigningKey if set signs the hash of the message.
     * @param recipientSharedEncryptionKey if set encrypts the message,  
     * hashes, and signs the hash, otherwise leaves un-encrypted and 
     * signs the hash  if the signature parameter @param senderSigningKey is not null
     * @return ApplicationMessage
     */
    public static ApplicationMessage userMessageToApplicationMessage(TransactionId transactionId,  byte[] message,  Ed25519PrivateKey senderSigningKey, String recipientSharedEncryptionKey){
        ApplicationMessageID applicationMessageID = ApplicationMessageID.newBuilder()
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
                .setApplicationMessageId(applicationMessageID);
        
        if(recipientSharedEncryptionKey == null) { // no encryption
            applicationMessageBuilder.setBusinessProcessMessage(ByteString.copyFrom(originalMessage));
            applicationMessage = applicationMessageBuilder.build();
        } else {
            try {
                // build one encrypted and one unecrypted message. Store the latter in the core db

                // Hash of unencrypted business message should be included in application message
                byte[] hashOfOriginalMessage = com.hedera.hcs.sxc.hashing.Hashing.sha(StringUtils.byteArrayToHexString(originalMessage));
                applicationMessageBuilder.setUnencryptedBusinessProcessMessageHash(ByteString.copyFrom(hashOfOriginalMessage));


                // Signature (using sender’s private key) of hash (above) should also be included in application message
               
                if(senderSigningKey!=null) { // signing may be turned off when override is used
                    byte[] sign = Signing.sign(hashOfOriginalMessage, senderSigningKey);
                    applicationMessageBuilder.setBusinessProcessSignatureOnHash(ByteString.copyFrom(sign));
                }

                // encrypt
                //String encryptionKey = recipientKeys.get("sharedSymmetricEncryptionKey");
                Class<?> messageEncryptionClass = Plugins.find("com.hedera.hcs.sxc.plugin.encryption.*", "com.hedera.hcs.sxc.interfaces.SxcMessageEncryption", true);
                SxcMessageEncryption encPlugin = (SxcMessageEncryption)messageEncryptionClass.newInstance();
                log.debug("Encrypting message with key " + recipientSharedEncryptionKey.substring(recipientSharedEncryptionKey.length()-10, recipientSharedEncryptionKey.length()-1));
                EncryptedData encryptedData = encPlugin.encrypt(
                        StringUtils.hexStringToByteArray(recipientSharedEncryptionKey)
                        , message);
                applicationMessageBuilder.setBusinessProcessMessage(
                        ByteString.copyFrom(encryptedData.getEncryptedData())
                );
                applicationMessageBuilder.setEncryptionRandom(
                        ByteString.copyFrom(encryptedData.getRandom())
                );

                applicationMessage = applicationMessageBuilder.build();

                
            } catch (Exception ex) {
                log.error(ex);
                
            }
        }                
       
        return applicationMessage;
    }
    
    
    public static List<ApplicationMessageChunk> chunk(ApplicationMessage applicationMessage) {
        List<ApplicationMessageChunk> parts = new ArrayList<>();
        if (applicationMessage != null) {
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
                        .setApplicationMessageId(applicationMessage.getApplicationMessageId())
                        .setChunkIndex(partId)
                        .setChunksCount(totalParts)
                        .setMessageChunk(ByteString.copyFrom(amMessageChunk))
                        .build();
    
                parts.add(applicationMessageChunk);
            }
        }
        return parts;
    }
}
