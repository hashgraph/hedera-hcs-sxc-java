package com.hedera.hcsapp.restclasses;

import java.util.List;

import com.hedera.hcsapp.entities.Credit;

import lombok.Data;

@Data
public final class SettlementProposal {
    
    private String threadId;
    private String applicationMessageId;
    private String payerName;
    private String recipientName;
    private String additionalNotes;
    private List<String> threadIds;
    private long netValue;
    private String currency;
    private String status;
    private String createdDate;
    private String createdTime;
    private List<Credit> credits;
}
