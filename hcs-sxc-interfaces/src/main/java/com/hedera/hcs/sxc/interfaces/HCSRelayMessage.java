package com.hedera.hcs.sxc.interfaces;


import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 *
 * A wrapper for messages as they arrive in the relay
 */
@Data
public class HCSRelayMessage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -53040925361901772L;
    private final Instant consensusTimestamp;
    private final byte[] message;
    private final byte[] runningHash;
    private final long sequenceNumber;
    private long topicShard = 0;
    private long topicRealm = 0;
    private long topicNum = 0;
    private int hash;

    public HCSRelayMessage(ConsensusMessage messagesResponse) {
        this.consensusTimestamp = messagesResponse.consensusTimestamp;
        this.message = messagesResponse.message;
        this.runningHash = messagesResponse.runningHash;
        this.sequenceNumber = messagesResponse.sequenceNumber;
        this.topicShard = messagesResponse.topicId.shard;
        this.topicRealm = messagesResponse.topicId.realm;
        this.topicNum = messagesResponse.topicId.topic;

        hash = 3;
        hash = 89 * hash + Objects.hashCode(messagesResponse);
        hash = 89 * hash + Objects.hashCode(messagesResponse.topicId);
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final HCSRelayMessage other = (HCSRelayMessage) obj;
//        if (!Objects.equals(this.topicMessagesResponse, other.topicMessagesResponse)) {
//            return false;
//        }
//        if (!Objects.equals(this.topicId, other.topicId)) {
//            return false;
//        }
//        return true;
//    }
}
