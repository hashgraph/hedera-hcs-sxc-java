package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public final class SettlementChannelProposal {

    private String threadId;
    private String additionalNotes;
    private String paymentChannelName;
}
