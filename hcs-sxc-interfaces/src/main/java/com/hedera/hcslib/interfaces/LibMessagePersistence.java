package com.hedera.hcslib.interfaces;

import com.hedera.hcslib.interfaces.MessagePersistenceLevel;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;

import java.util.List;
import com.hedera.hcslib.proto.java.TransactionID;

public interface LibMessagePersistence {
    void storeMessage(MessagePersistenceLevel level, ApplicationMessage.Builder messagesResponse);
    public List<ApplicationMessageChunk> getParts(TransactionID applicationMessageId);
    public void putChunks(TransactionID applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeChunks(TransactionID messageEnvelopeId);
}
