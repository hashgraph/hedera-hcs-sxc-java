package com.hedera.hcs.sxc.interfaces;

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
