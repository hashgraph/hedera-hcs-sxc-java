package com.hedera.hcsapp.restclasses;

import java.util.List;

import lombok.Data;

@Data
public final class SettlementProposal {
    
    private String payerName;
    private String recipientName;
    private String additionalNotes;
    private List<String> threadIds;
    private long netValue;
    private String currency;
}
