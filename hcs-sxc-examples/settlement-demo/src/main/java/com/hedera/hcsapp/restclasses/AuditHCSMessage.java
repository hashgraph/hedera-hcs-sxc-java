package com.hedera.hcsapp.restclasses;

import com.hedera.hcsapp.AppData;

import lombok.Data;

@Data
public class AuditHCSMessage {

    private long consensusTimeStampSeconds;
    private int consensusTimeStampNanos;
    private String runningHash;
    private long sequenceNumber;
    private String message;
    private String part;
    private String topicId;
    
    public AuditHCSMessage(AppData appData) {
        this.topicId = appData.getHCSLib().getTopicIds().get(appData.getTopicIndex()).toString();
    }
}
