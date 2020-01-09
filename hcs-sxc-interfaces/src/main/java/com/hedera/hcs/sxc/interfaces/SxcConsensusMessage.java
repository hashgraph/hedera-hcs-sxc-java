package com.hedera.hcs.sxc.interfaces;

import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;

import lombok.Data;

@Data
public class SxcConsensusMessage {
    private String topicId;
    private Long consensusTimeStampSeconds;
    private int consensusTimeStampNanos;
    private byte[] message;
    private byte[] runningHash;
    private long sequenceNumber;
    
    public SxcConsensusMessage() {
        
    }
    
    public SxcConsensusMessage(ConsensusMessage mirrorTopicMessageResponse) {
        this.consensusTimeStampSeconds = mirrorTopicMessageResponse.consensusTimestamp.getEpochSecond();
        this.consensusTimeStampNanos = mirrorTopicMessageResponse.consensusTimestamp.getNano();
        this.message = mirrorTopicMessageResponse.message;
        this.runningHash = mirrorTopicMessageResponse.runningHash;
        this.sequenceNumber = mirrorTopicMessageResponse.sequenceNumber;
        this.topicId = mirrorTopicMessageResponse.topicId.toString();
    }
}
