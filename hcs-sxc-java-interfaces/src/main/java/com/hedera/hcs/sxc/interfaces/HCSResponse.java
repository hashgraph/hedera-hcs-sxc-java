package com.hedera.hcs.sxc.interfaces;

import com.hedera.hcs.sxc.proto.ApplicationMessageId;

public class HCSResponse {
    private ApplicationMessageId applicationMessageId;
    private byte[] message;
    
    public ApplicationMessageId getApplicationMessageId() {
        return this.applicationMessageId;
    }
    public void setApplicationMessageId(ApplicationMessageId applicationMessageId) {
        this.applicationMessageId = applicationMessageId;
    }
    public byte[] getMessage() {
        return this.message;
    }
    public void setMessage(byte[] message) {
        this.message = message.clone();
    }
}
