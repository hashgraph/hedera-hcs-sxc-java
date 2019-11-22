package com.hedera.hcsapp.notifications;

public class NotificationMessage {
 
    private String recipient;
    private String payer;
    private String threadId;
    private String context;

    public String getRecipient() {
        return this.recipient;
    }
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getPayer() {
        return this.payer;
    }
    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getThreadId() {
        return this.threadId;
    }
    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getContext() {
        return this.context;
    }
    public void setContext(String context) {
        this.context = context;
    }
    
}