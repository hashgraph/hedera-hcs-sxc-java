package com.hedera.plugin.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "MirrorResponses")
public class MirrorResponse {
    @Id
    private String timestamp;
    private Long timestampNS;
    private Long timestampSeconds;
    private int timestampNanos;
    private byte[] message;
    private String topicId;
    private byte[] runningHash;
    private long sequenceNumber;
    
}
