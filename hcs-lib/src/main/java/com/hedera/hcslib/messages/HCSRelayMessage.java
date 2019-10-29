/*
 *  Copyirght hash-hash.info
 */
package com.hedera.hcslib.messages;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * A wrapper for messages as they arrive in the relay
 */
public class HCSRelayMessage implements Serializable {
    private MirrorGetTopicMessages.MirrorGetTopicMessagesResponse topicMessagesResponse;
    private TopicId topicId;
    
    public HCSRelayMessage(MirrorGetTopicMessages.MirrorGetTopicMessagesResponse messagesResponse, TopicId topicId) {
        this.topicMessagesResponse = messagesResponse;
        this.topicId = topicId;
    }

    public MirrorGetTopicMessages.MirrorGetTopicMessagesResponse getTopicMessagesResponse() {
        return topicMessagesResponse;
    }

    public void setTopicMessagesResponse(MirrorGetTopicMessages.MirrorGetTopicMessagesResponse topicMessagesResponse) {
        this.topicMessagesResponse = topicMessagesResponse;
    }

    public TopicId getTopicId() {
        return topicId;
    }

    public void setTopicId(TopicId topicId) {
        this.topicId = topicId;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.topicMessagesResponse);
        hash = 89 * hash + Objects.hashCode(this.topicId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HCSRelayMessage other = (HCSRelayMessage) obj;
        if (!Objects.equals(this.topicMessagesResponse, other.topicMessagesResponse)) {
            return false;
        }
        if (!Objects.equals(this.topicId, other.topicId)) {
            return false;
        }
        return true;
    }
    
    
    
}
