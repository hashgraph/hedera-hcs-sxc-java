package com.hedera.hcs.sxc.callback;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hcs.sxc.interfaces.HCSCallBackToAppInterface;
import com.hedera.hcs.sxc.interfaces.HCSResponse;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface;
import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;
import com.hedera.hcs.sxc.plugins.Plugins;
import com.hedera.hcs.sxc.utils.ByteUtil;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Implements callback registration and notification capabilities to support apps
 *
 */
public final class OnHCSMessageCallback implements HCSCallBackFromMirror {

    private final List<HCSCallBackToAppInterface> observers = new ArrayList<>();
    private HCSCore hcsCore;
    private  boolean signMessages;
    private  boolean encryptMessages;
    private  boolean rotateKeys;
    private Class<?> messageEncryptionClass;
    private SxcMessageEncryption messageEncryptionPlugin;
    
    public OnHCSMessageCallback (HCSCore hcsCore) throws Exception {
        this.hcsCore = hcsCore;
        
        this.signMessages = hcsCore.getSignMessages();
        this.encryptMessages = hcsCore.getEncryptMessages();
        this.rotateKeys = hcsCore.getRotateKeys();
        
        if(this.signMessages){
            
        }
        if (this.encryptMessages){
            messageEncryptionClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcMessageEncryption", true);
            this.messageEncryptionPlugin = (SxcMessageEncryption)messageEncryptionClass.newInstance();
        }
        if(this.rotateKeys){
            
        }
        
        
        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.hcs.sxc.plugin.persistence.*", "com.hedera.hcs.sxc.interfaces.SxcMessagePersistence", true);
        this.hcsCore.setMessagePersistence((SxcMessagePersistence)persistenceClass.newInstance());
        this.hcsCore.getMessagePersistence().setHibernateProperties(this.hcsCore.getHibernateConfig());

        // load mirror callback implementation at runtime
        Class<?> callbackClass = Plugins.find("com.hedera.hcs.sxc.plugin.mirror.*", "com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface", true);
        MirrorSubscriptionInterface mirrorSubscription = ((MirrorSubscriptionInterface)callbackClass.newInstance());

        if (this.hcsCore.getCatchupHistory()) {
            Optional<Instant> lastConsensusTimestamp = Optional.of(this.hcsCore.getMessagePersistence().getLastConsensusTimestamp());
            mirrorSubscription.init(this, this.hcsCore.getApplicationId(), lastConsensusTimestamp, this.hcsCore.getMirrorAddress(), this.hcsCore.getConsensusTopicIds());
        } else {
            mirrorSubscription.init(this, this.hcsCore.getApplicationId(), Optional.empty(), this.hcsCore.getMirrorAddress(), this.hcsCore.getConsensusTopicIds());
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
    @Override
    public void notifyObservers(byte[] message, ApplicationMessageId applicationMessageId) {
        HCSResponse hcsResponse = new HCSResponse();
        hcsResponse.setApplicationMessageId(applicationMessageId);
    
        
        hcsResponse.setMessage(message);
        observers.forEach(listener -> listener.onMessage(hcsResponse));
    }
    
    @Override
    public void storeMirrorResponse(SxcConsensusMessage consensusMessage) {
        hcsCore.getMessagePersistence().storeMirrorResponse(consensusMessage);
    }
    @Override
    public void partialMessage(ApplicationMessageChunk messagePart) throws InvalidProtocolBufferException{

        Optional<ApplicationMessage> messageEnvelopeOptional =
                pushUntilCompleteMessage(messagePart, this.hcsCore.getMessagePersistence());

        if (messageEnvelopeOptional.isPresent()){ // is present if all parts received
            this.hcsCore.getMessagePersistence().storeApplicationMessage(messageEnvelopeOptional.get().getApplicationMessageId(), messageEnvelopeOptional.get());
                
            ApplicationMessage appMessage = messageEnvelopeOptional.get();
            
            if(this.encryptMessages){
                try {
                    ApplicationMessage decryptedAppmessage = ApplicationMessage.newBuilder()
                            .setApplicationMessageId(appMessage.getApplicationMessageId())
                            .setBusinessProcessHash(appMessage.getBusinessProcessHash())
                            .setBusinessProcessMessage( 
                                    ByteString.copyFrom(
                                            this.messageEncryptionPlugin.decrypt(hcsCore.getMessageEncryptionKey(), appMessage.getBusinessProcessMessage().toByteArray())
                                    )
                            )
                            .setBusinessProcessSignature(appMessage.getBusinessProcessSignature())
                            .build();
                            appMessage = decryptedAppmessage;
                } catch (Exception ex) {
                    Logger.getLogger(OnHCSMessageCallback.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
            notifyObservers( appMessage.toByteArray(), appMessage.getApplicationMessageId());
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
    static Optional<ApplicationMessage> pushUntilCompleteMessage(ApplicationMessageChunk messageChunk, SxcMessagePersistence persistence) throws InvalidProtocolBufferException {

        ApplicationMessageId applicationMessageId = messageChunk.getApplicationMessageId();
        //look up db to find parts received already
        List<ApplicationMessageChunk> chunkList = persistence.getParts(applicationMessageId);
        if (chunkList==null){
            chunkList = new ArrayList<ApplicationMessageChunk>();
            chunkList.add(messageChunk);
        } else {
            chunkList.add(messageChunk);
        }
        persistence.putParts(applicationMessageId, chunkList);

        if (messageChunk.getChunksCount() == 1){
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
