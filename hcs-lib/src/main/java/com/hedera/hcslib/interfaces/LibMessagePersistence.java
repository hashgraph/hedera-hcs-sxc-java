package com.hedera.hcslib.interfaces;

import com.hedera.hcslib.proto.java.MessageEnvelope;
import java.util.List;

import com.hedera.hcslib.proto.java.MessagePart;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

public interface LibMessagePersistence {
    void storeMessage(MessagePersistenceLevel level, MessageEnvelope.Builder messagesResponse);
    
}
