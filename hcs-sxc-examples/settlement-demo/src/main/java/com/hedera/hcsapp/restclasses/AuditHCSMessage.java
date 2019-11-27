package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public class AuditHCSMessage {

    private long consensusTimeStampSeconds;
    private int consensusTimeStampNanos;
    private String runningHash;
    private long sequenceNumber;
    private String message;
}
