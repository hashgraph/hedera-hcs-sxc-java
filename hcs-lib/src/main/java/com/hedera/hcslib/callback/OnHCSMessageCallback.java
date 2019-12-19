package com.hedera.hcslib.callback;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.interfaces.HCSCallBackFromMirror;
import com.hedera.hcslib.interfaces.HCSCallBackToAppInterface;
import com.hedera.hcslib.interfaces.HCSResponse;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MirrorSubscriptionInterface;
import com.hedera.hcslib.plugins.Plugins;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.hcslib.utils.ByteUtil;
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
    private HCSLib hcsLib;

    public OnHCSMessageCallback (HCSLib hcsLib) throws Exception {
        this.hcsLib = hcsLib;
        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.plugin.persistence.*", "com.hedera.hcslib.interfaces.LibMessagePersistence", true);
        hcsLib.setMessagePersistence((LibMessagePersistence)persistenceClass.newInstance());

        // load mirror callback implementation at runtime
        Class<?> callbackClass = Plugins.find("com.hedera.plugin.mirror.*", "com.hedera.hcslib.interfaces.MirrorSubscriptionInterface", true);
        MirrorSubscriptionInterface mirrorSubscription = ((MirrorSubscriptionInterface)callbackClass.newInstance());

        if (hcsLib.getCatchupHistory()) {
            Optional<Instant> lastConsensusTimestamp = Optional.of(hcsLib.getMessagePersistence().getLastConsensusTimestamp());
            mirrorSubscription.init(this, this.hcsLib.getApplicationId(), lastConsensusTimestamp, this.hcsLib.getMirrorAddress(), this.hcsLib.getTopicIds(), this.hcsLib.getMirrorReconnectDelay());
        } else {
            mirrorSubscription.init(this, this.hcsLib.getApplicationId(), Optional.empty(), this.hcsLib.getMirrorAddress(), this.hcsLib.getTopicIds(), this.hcsLib.getMirrorReconnectDelay());
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
        hcsLib.getMessagePersistence().storeMirrorResponse(consensusMessage);
    }
    public void partialMessage(ApplicationMessageChunk messagePart) throws InvalidProtocolBufferException {

        Optional<ApplicationMessage> messageEnvelopeOptional =
                pushUntilCompleteMessage(messagePart, hcsLib.getMessagePersistence());

        if (messageEnvelopeOptional.isPresent()){
            hcsLib.getMessagePersistence().storeApplicationMessage(messageEnvelopeOptional.get().getApplicationMessageId(), messageEnvelopeOptional.get());
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
    static Optional<ApplicationMessage> pushUntilCompleteMessage(ApplicationMessageChunk messageChunk, LibMessagePersistence persistence) throws InvalidProtocolBufferException {

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
