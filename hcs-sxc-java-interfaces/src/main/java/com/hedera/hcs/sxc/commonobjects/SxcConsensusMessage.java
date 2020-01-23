package com.hedera.hcs.sxc.commonobjects;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

public class SxcConsensusMessage {
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
