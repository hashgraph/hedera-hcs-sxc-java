package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public final class SettlementPaymentInit {

    private String threadId;
    private String payerAccountDetails;
    private String recipientAccountDetails;
    private String additionalNotes;
}
