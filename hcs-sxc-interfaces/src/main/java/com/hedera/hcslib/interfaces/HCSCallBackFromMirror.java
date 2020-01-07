package com.hedera.hcslib.interfaces;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.ApplicationMessageId;

public interface HCSCallBackFromMirror {
    public void addObserver(HCSCallBackToAppInterface listener);
    public void notifyObservers(byte[] message, ApplicationMessageId applicationMessageId);
    public void storeMirrorResponse(ConsensusMessage consensusMessage);
    public void partialMessage(ApplicationMessageChunk messagePart) throws InvalidProtocolBufferException;
}
