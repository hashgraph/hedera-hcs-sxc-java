package com.hedera.hcs.sxc.queue;

import lombok.Data;

@Data
public class HCSMessageRest {
    private String consensusTimeStamp;
    private String message;
    private String runningHash;
    private long sequence;
}
