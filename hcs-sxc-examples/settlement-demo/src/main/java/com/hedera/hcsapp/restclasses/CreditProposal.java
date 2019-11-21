package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public final class CreditProposal {

    private String payerName;
    private String recipientName;
    private String reference;
    private long amount;
    private String currency;
    private String additionalNotes;
}
