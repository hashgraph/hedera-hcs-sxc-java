package com.hedera.hcslib.interfaces;


import com.hedera.hcslib.proto.java.ApplicationMessageChunk;

import java.util.List;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages;

public interface LibMessagePersistence {
    void storeMessage(MessagePersistenceLevel level, MirrorGetTopicMessages.MirrorGetTopicMessagesResponse.Builder  messagesResponse);
    public List<ApplicationMessageChunk> getParts(ApplicationMessageId applicationMessageId);
    public void putChunks(ApplicationMessageId applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeChunks(ApplicationMessageId messageEnvelopeId);
}
