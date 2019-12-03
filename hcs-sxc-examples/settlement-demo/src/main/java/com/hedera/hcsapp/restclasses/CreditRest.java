package com.hedera.hcsapp.restclasses;

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.entities.Credit;

import lombok.Data;

@Data
public final class CreditRest {

    private String threadId;
    private String applicationMessageId;
    private String payerName;
    private String recipientName;
    private String reference;
    private long amount;
    private String currency;
    private String additionalNotes;
    private String status;
    private String createdDate;
    private String createdTime;
    private String topicId;
    
    public CreditRest(Credit credit, AppData appData) {
        this.threadId = credit.getThreadId();
        this.applicationMessageId = credit.getApplicationMessageId();
        this.payerName = credit.getPayerName();
        this.recipientName = credit.getRecipientName();
        this.reference = credit.getReference();
        this.amount = credit.getAmount();
        this.currency = credit.getCurrency();
        this.additionalNotes = credit.getAdditionalNotes();
        this.status = credit.getStatus();
        this.createdDate = credit.getCreatedDate();
        this.createdTime = credit.getCreatedTime();
        this.topicId = appData.getHCSLib().getTopicIds().get(appData.getTopicIndex()).toString();
    }
}