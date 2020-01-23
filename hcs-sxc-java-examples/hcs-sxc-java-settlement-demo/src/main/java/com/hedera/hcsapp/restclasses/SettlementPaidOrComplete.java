package com.hedera.hcsapp.restclasses;

import lombok.Data;

@Data
public final class SettlementPaidOrComplete {
    
    private String threadId;
    private String additionalNotes;
}
