package com.hedera.hcs.sxc.callback;

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
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.config.Topic;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hcs.sxc.interfaces.HCSCallBackToAppInterface;
import com.hedera.hcs.sxc.interfaces.HCSResponse;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface;
import com.hedera.hcs.sxc.interfaces.SxcKeyRotation;
import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;
import com.hedera.hcs.sxc.plugins.Plugins;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.utils.ByteUtil;

import lombok.extern.log4j.Log4j2;

import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;
import com.hedera.hcs.sxc.proto.KeyRotationInitialise;
import com.hedera.hcs.sxc.proto.KeyRotationRespond;
import com.hedera.hcs.sxc.proto.Timestamp;
import java.time.Duration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.crypto.KeyAgreement;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * Implements callback registration and notification capabilities to support apps
 *
 */
@Log4j2
public final class OnHCSMessageCallback implements HCSCallBackFromMirror {

    private final List<HCSCallBackToAppInterface> observers = new ArrayList<>();
    private HCSCore hcsCore;
    private  boolean signMessages;
    private  boolean encryptMessages;
    private  boolean rotateKeys;
    private Class<?> messageEncryptionClass;
    private SxcMessageEncryption messageEncryptionPlugin;
    private  List<Topic> topics;
    private SxcKeyRotation keyRotationPlugin;
    
    public OnHCSMessageCallback (HCSCore hcsCore) throws Exception {
        this.hcsCore = hcsCore;
        
        this.signMessages = hcsCore.getSignMessages();
        this.encryptMessages = hcsCore.getEncryptMessages();
        this.rotateKeys = hcsCore.getRotateKeys();
        this.topics = hcsCore.getTopics();
        
        if(this.signMessages){
            
        }
        if (this.encryptMessages){
            messageEncryptionClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcMessageEncryption", true);
            this.messageEncryptionPlugin = (SxcMessageEncryption)messageEncryptionClass.newInstance();
        }
         if(this.rotateKeys){
            Class<?> messageKeyRotationClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcKeyRotation", true);
            this.keyRotationPlugin = (SxcKeyRotation)messageKeyRotationClass.newInstance();
        }
        
        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.hcs.sxc.plugin.persistence.*", "com.hedera.hcs.sxc.interfaces.SxcPersistence", true);
        this.hcsCore.setMessagePersistence((SxcPersistence)persistenceClass.newInstance());
        this.hcsCore.getMessagePersistence().setHibernateProperties(this.hcsCore.getHibernateConfig());

        // load mirror callback implementation at runtime
        Class<?> callbackClass = Plugins.find("com.hedera.hcs.sxc.plugin.mirror.*", "com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface", true);
        MirrorSubscriptionInterface mirrorSubscription = ((MirrorSubscriptionInterface)callbackClass.newInstance());

        if (this.hcsCore.getCatchupHistory()) {
            log.info("catching up with mirror history");
            Optional<Instant> lastConsensusTimestamp = Optional.of(this.hcsCore.getMessagePersistence().getLastConsensusTimestamp());
            mirrorSubscription.init(this, this.hcsCore.getApplicationId(), lastConsensusTimestamp, this.hcsCore.getMirrorAddress(), this.hcsCore.getConsensusTopicIds());
        } else {
            log.info("NOT catching up with mirror history");
            mirrorSubscription.init(this, this.hcsCore.getApplicationId(), Optional.of(Instant.now()), this.hcsCore.getMirrorAddress(), this.hcsCore.getConsensusTopicIds());
        }
    }
    /**
     * Adds an observer to the list of observers
     * @param listener
     */
    @Override
    public void addObserver(HCSCallBackToAppInterface listener) {
       observers.add(listener);
    }
    /**
     * Notifies all observers with the supplied message
     * @param message
     * @param applicationMessageId
     */
    public void notifyObservers(byte[] message, ApplicationMessageID applicationMessageId) {
        HCSResponse hcsResponse = new HCSResponse();
        hcsResponse.setApplicationMessageID(applicationMessageId);
        hcsResponse.setMessage(message);
        observers.forEach(listener -> listener.onMessage(hcsResponse));
    }
    public void storeMirrorResponse(SxcConsensusMessage consensusMessage) {
        hcsCore.getMessagePersistence().storeMirrorResponse(consensusMessage);
    }
    
    @Override
    public void partialMessage(ApplicationMessageChunk messagePart) {
                
        try {
            Optional<ApplicationMessage> messageEnvelopeOptional =
                    pushUntilCompleteMessage(messagePart, this.hcsCore.getMessagePersistence());
            
            if (messageEnvelopeOptional.isPresent()){ // is present if all parts received
                
                // TODO check signature to test if message is for me, no need to decrypt
                boolean isMessageForMe = true;
                if (isMessageForMe == false) return; 
                
                ApplicationMessage appMessage = messageEnvelopeOptional.get();

                if(this.encryptMessages){
                   
                    try {
                        
                        byte[] decryptedBPM = this.messageEncryptionPlugin.decrypt(hcsCore.getMessageEncryptionKey(), appMessage.getBusinessProcessMessage().toByteArray());
                        
                        Any any = Any.parseFrom(decryptedBPM);
                        
                        if(any.is(KeyRotationInitialise.class)){  //======= KR1==========================================
                            // an init message has arrived
                            // check if it was me who's the initiator. Skip msg if so.
                            boolean isMeInitiator = hcsCore.getTempKeyAgreement() != null;
                            
                            if ( ! isMeInitiator ) {
                            
                                KeyRotationInitialise kr1 = any.unpack(KeyRotationInitialise.class);
                                byte[] initiatorPublicKeyEncoded = kr1.getInitiatorPublicKeyEncoded().toByteArray();

                                // create your own new key store it and respond
                                Pair<byte[], byte[]> respond = keyRotationPlugin.respond(initiatorPublicKeyEncoded);

                                byte[] newPublicKey =  respond.getLeft();
                                byte[] newSecretKey = respond.getRight();  
                                byte[] oldSecretKey = hcsCore.getMessageEncryptionKey();
                                hcsCore.updateSecretKey(newSecretKey);
                                hcsCore.getMessagePersistence().storeSecretKey(newSecretKey);


                                KeyRotationRespond kr2 = KeyRotationRespond.newBuilder()
                                    .setResponderPublicKeyEncoded(ByteString.copyFrom(newPublicKey))
                                    .build();

                                // prepare the response and send it over to the initiator

                                Any anyPack = Any.pack(kr2);
                                byte[] encryptedAnyPackedChunkBody = messageEncryptionPlugin.encrypt(oldSecretKey, anyPack.toByteArray());

                                
                                TransactionId transactionId = new TransactionId(hcsCore.getOperatorAccountId());
                                ApplicationMessageID newAppId = ApplicationMessageID.newBuilder()
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
                                    .setTopicId(this.topics.get(
                                            0 // TODO get the topic from the appMessage
                                    ).getConsensusTopicId())
                                    .setTransactionId(transactionId);

                                // submit to network

                                try (Client client = new Client(hcsCore.getNodeMap())) {
                                    client.setOperator(
                                            hcsCore.getOperatorAccountId(),
                                             hcsCore.getEd25519PrivateKey()
                                    );

                                    client.setMaxTransactionFee(hcsCore.getMaxTransactionFee());

                                    TransactionId txIdKR2 =  txRotation.execute(client);

                                    TransactionReceipt receiptKR2 = txIdKR2.getReceipt(client, Duration.ofSeconds(30));

                                } catch (HederaStatusException ex) {
                                        log.error(ex);
                                } catch (HederaNetworkException ex) {
                                    log.error(ex);
                                }
                                    
                            } // test checking if initiator
                        } // end any test initialise
                        
                        if(any.is(KeyRotationRespond.class)){ // ============ KR2 =====================================================
                            
                            // a respond message has arrived
                            // check if it was me who sent respond message. Skip msg if so.
                            boolean isMeResponder = hcsCore.getTempKeyAgreement() != null;
                            
                            if ( isMeResponder ) {
                            
                                KeyRotationRespond kr2 = any.unpack(KeyRotationRespond.class);
                                byte[] responderPublicKeyEncoded = kr2.getResponderPublicKeyEncoded().toByteArray();

                                // get the keyAgreement from core
                                KeyAgreement keyAgreement = hcsCore.getTempKeyAgreement();
                                hcsCore.setTempKeyAgreement(null);
                                byte[] newSecretKey = keyRotationPlugin.finalise(responderPublicKeyEncoded, keyAgreement);
                                  // store new SecretKey in Database
                                hcsCore.updateSecretKey(newSecretKey);
                                hcsCore.getMessagePersistence().storeSecretKey(newSecretKey);
                                
                            }
                        } else {
                                ApplicationMessage decryptedAppmessage = ApplicationMessage.newBuilder()
                                    .setApplicationMessageId(appMessage.getApplicationMessageId())
                                    .setBusinessProcessHash(appMessage.getBusinessProcessHash())
                                    .setBusinessProcessMessage( 
                                            ByteString.copyFrom(decryptedBPM)
                                     )
                                    .setBusinessProcessSignature(appMessage.getBusinessProcessSignature())
                                    .build();
                                appMessage = decryptedAppmessage;
                                this.hcsCore.getMessagePersistence().storeApplicationMessage(appMessage.getApplicationMessageId(), appMessage);

                        }
                        //skip storing
                        notifyObservers( appMessage.toByteArray(), appMessage.getApplicationMessageId());
            
                    } catch (Exception e){
                        e.printStackTrace();
                    }  
                    
                } else { // not encrypted
                    this.hcsCore.getMessagePersistence().storeApplicationMessage(messageEnvelopeOptional.get().getApplicationMessageId(), messageEnvelopeOptional.get());
                    notifyObservers( appMessage.toByteArray(), appMessage.getApplicationMessageId());
                }
                
            } else { // message envelope not present
                // do nothing - there are still parts that need to be collected.
            }
      
        } catch (InvalidProtocolBufferException ex) {
            log.error(ex);
        }     
    }
    
    /**
     * Adds ApplicationMessageChunk into memory and returns
     * a fully combined / assembled ApplicationMessage if all parts are present
     * @param messageChunk a chunked message received from the queue
     * @param persistence the memory. The object is side-effected with each
     * function invocation.
     * @return a fully combined / assembled ApplicationMessage if all parts present,
     * nothing otherwise.
     * @throws InvalidProtocolBufferException
     */
    static Optional<ApplicationMessage> pushUntilCompleteMessage(ApplicationMessageChunk messageChunk, SxcPersistence persistence) throws InvalidProtocolBufferException {

        ApplicationMessageID applicationMessageId = messageChunk.getApplicationMessageId();
        //look up db to find parts received already
        List<ApplicationMessageChunk> chunkList = persistence.getParts(applicationMessageId);
        if (chunkList==null){
            chunkList = new ArrayList<ApplicationMessageChunk>();
            chunkList.add(messageChunk);
        } else {
            chunkList.add(messageChunk);
        }
        persistence.putParts(applicationMessageId, chunkList);

        if (messageChunk.getChunksCount() == 1){ // if it's only one then an entire ApplicationMessage did fit in the MessageChunk
                ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(messageChunk.getMessageChunk());
                return  Optional.of( applicationMessage);
        } else if (chunkList.size() == messageChunk.getChunksCount()) { // all parts received
                // sort by part id
                chunkList.sort(Comparator.comparingInt(ApplicationMessageChunk::getChunkIndex));
                // merge down
                ByteString merged =
                        chunkList.stream()
                                .map(ApplicationMessageChunk::getMessageChunk)
                                .reduce(ByteUtil::merge).get();
                // construct envelope from merged array. TODO: if fail
                ApplicationMessage messageEnvelope = ApplicationMessage.parseFrom(merged);
                return  Optional.of(messageEnvelope);
        } else { // not all parts received yet
            return Optional.empty();
        }
    }
}