package com.hedera.hcslib.consensus;

import com.hedera.hcslib.proto.java.TransactionID;

public class HCSResponse {
    private TransactionID applicationMessageId;
    private byte[] message;
    
    public TransactionID getApplicationMessageId() {
        return this.applicationMessageId;
    }
    public void setApplicationMessageId(TransactionID applicationMessageId) {
        this.applicationMessageId = applicationMessageId;
    }
    public byte[] getMessage() {
        return this.message;
    }
    public void setMessage(byte[] message) {
        this.message = message.clone();
    }
}
