package com.hedera.hcs.sxc.interfaces;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageId;

public interface HCSCallBackFromMirror {
    public void addObserver(HCSCallBackToAppInterface listener);
    public void notifyObservers(byte[] message, ApplicationMessageId applicationMessageId);
    public void storeMirrorResponse(SxcConsensusMessage consensusMessage);
    public void partialMessage(ApplicationMessageChunk messagePart) throws InvalidProtocolBufferException;
}
