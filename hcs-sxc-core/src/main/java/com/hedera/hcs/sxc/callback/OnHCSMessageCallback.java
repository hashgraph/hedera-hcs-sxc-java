package com.hedera.hcs.sxc.callback;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hcs.sxc.interfaces.HCSCallBackToAppInterface;
import com.hedera.hcs.sxc.interfaces.HCSResponse;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface;
import com.hedera.hcs.sxc.plugins.Plugins;
import com.hedera.hcs.sxc.utils.ByteUtil;
import com.hedera.hcs.sxc.proto.java.ApplicationMessage;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageId;

import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 *
 * Implements callback registration and notification capabilities to support apps
 *
 */
@Log4j2
public final class OnHCSMessageCallback implements HCSCallBackFromMirror {

    private final List<HCSCallBackToAppInterface> observers = new ArrayList<>();
    private HCSCore hcsCore;

    public OnHCSMessageCallback (HCSCore hcsCore) throws Exception {
        this.hcsCore = hcsCore;
        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.hcs.sxc.plugin.persistence.*", "com.hedera.hcs.sxc.interfaces.SxcMessagePersistence", true);
        this.hcsCore.setMessagePersistence((SxcMessagePersistence)persistenceClass.newInstance());

        // load mirror callback implementation at runtime
        Class<?> callbackClass = Plugins.find("com.hedera.hcs.sxc.plugin.mirror.*", "com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface", true);
        MirrorSubscriptionInterface mirrorSubscription = ((MirrorSubscriptionInterface)callbackClass.newInstance());

        if (this.hcsCore.getCatchupHistory()) {
            Optional<Instant> lastConsensusTimestamp = Optional.of(this.hcsCore.getMessagePersistence().getLastConsensusTimestamp());
            mirrorSubscription.init(this, this.hcsCore.getApplicationId(), lastConsensusTimestamp, this.hcsCore.getMirrorAddress(), this.hcsCore.getTopicIds(), this.hcsCore.getMirrorReconnectDelay());
        } else {
            mirrorSubscription.init(this, this.hcsCore.getApplicationId(), Optional.empty(), this.hcsCore.getMirrorAddress(), this.hcsCore.getTopicIds(), this.hcsCore.getMirrorReconnectDelay());
        }
    }
    /**
     * Adds an observer to the list of observers
     * @param listener
     */
    public void addObserver(HCSCallBackToAppInterface listener) {
       observers.add(listener);
    }
    /**
     * Notifies all observers with the supplied message
     * @param message
     */
    public void notifyObservers(byte[] message, ApplicationMessageId applicationMessageId) {
        HCSResponse hcsResponse = new HCSResponse();
        hcsResponse.setApplicationMessageId(applicationMessageId);
        hcsResponse.setMessage(message);
        observers.forEach(listener -> listener.onMessage(hcsResponse));
    }
    public void storeMirrorResponse(ConsensusMessage consensusMessage) {
        hcsCore.getMessagePersistence().storeMirrorResponse(consensusMessage);
    }
    public void partialMessage(ApplicationMessageChunk messagePart) throws InvalidProtocolBufferException {

        Optional<ApplicationMessage> messageEnvelopeOptional =
                pushUntilCompleteMessage(messagePart, this.hcsCore.getMessagePersistence());

        if (messageEnvelopeOptional.isPresent()){
            this.hcsCore.getMessagePersistence().storeApplicationMessage(messageEnvelopeOptional.get().getApplicationMessageId(), messageEnvelopeOptional.get());
            notifyObservers( messageEnvelopeOptional.get().toByteArray(), messageEnvelopeOptional.get().getApplicationMessageId());
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
