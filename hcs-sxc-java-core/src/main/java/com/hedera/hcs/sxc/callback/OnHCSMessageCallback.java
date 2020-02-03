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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hcs.sxc.interfaces.HCSCallBackToAppInterface;
import com.hedera.hcs.sxc.interfaces.HCSResponse;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface;
import com.hedera.hcs.sxc.plugins.Plugins;
import com.hedera.hcs.sxc.utils.ByteUtil;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;

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
public final class OnHCSMessageCallback implements HCSCallBackFromMirror {

    private final List<HCSCallBackToAppInterface> observers = new ArrayList<>();
    private HCSCore hcsCore;

    public OnHCSMessageCallback (HCSCore hcsCore) throws Exception {
        this.hcsCore = hcsCore;
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
    public void addObserver(HCSCallBackToAppInterface listener) {
       observers.add(listener);
    }
    /**
     * Notifies all observers with the supplied message
     * @param message
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
