package com.hedera.hcs.sxc.commonobjects;

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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import java.io.Serializable;

public class SxcConsensusMessage implements Serializable {
    public final ConsensusTopicId topicId;

    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    public SxcConsensusMessage(ConsensusTopicId topicId, ConsensusTopicResponse message) {
        this.topicId = topicId;
        this.consensusTimestamp = TimestampHelper.timestampTo(message.getConsensusTimestamp());
        this.message = message.getMessage().toByteArray();
        this.runningHash = message.getRunningHash().toByteArray();
        this.sequenceNumber = message.getSequenceNumber();
    }

    public SxcConsensusMessage(String topicId, ConsensusTopicResponse message) {
        this(ConsensusTopicId.fromString(topicId), message);
    }

    public String getMessageString() {
        return new String(message, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "ConsensusMessage{"
            + "topicId=" + topicId
            + ", consensusTimestamp=" + consensusTimestamp
            + ", message=" + Arrays.toString(message)
            + ", runningHash=" + Arrays.toString(runningHash)
            + ", sequenceNumber=" + sequenceNumber
            + '}';
    }
}
