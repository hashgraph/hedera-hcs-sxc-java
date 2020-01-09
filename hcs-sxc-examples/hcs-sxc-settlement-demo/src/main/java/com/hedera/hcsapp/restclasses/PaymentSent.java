package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public final class PaymentSent {

    private String threadId;
    private String paymentReference;
    private String additionalNotes;
}
