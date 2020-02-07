package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SettlementPaidOrCompleteTest {
    
    @Test
    public void testSettlemePaidOrComplete() {
        SettlementPaidOrComplete settlementPaidOrComplete = new SettlementPaidOrComplete();
        
        settlementPaidOrComplete.setAdditionalNotes("additionalNotes");
        settlementPaidOrComplete.setThreadId("threadId");
        
        assertEquals("additionalNotes", settlementPaidOrComplete.getAdditionalNotes());
        assertEquals("threadId", settlementPaidOrComplete.getThreadId());
    }
}
