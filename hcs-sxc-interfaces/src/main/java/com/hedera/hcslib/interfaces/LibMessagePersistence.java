package com.hedera.hcslib.interfaces;


import com.hedera.hcslib.proto.java.ApplicationMessageChunk;

import java.util.List;
import com.hedera.hcslib.proto.java.TransactionID;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages;

public interface LibMessagePersistence {
    void storeMessage(MessagePersistenceLevel level, MirrorGetTopicMessages.MirrorGetTopicMessagesResponse.Builder  messagesResponse);
    public List<ApplicationMessageChunk> getParts(TransactionID applicationMessageId);
    public void putChunks(TransactionID applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeChunks(TransactionID messageEnvelopeId);
}
