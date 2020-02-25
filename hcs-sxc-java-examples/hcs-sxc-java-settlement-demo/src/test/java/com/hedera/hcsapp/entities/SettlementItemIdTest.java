package com.hedera.hcsapp.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class SettlementItemIdTest {
    
    @Test
    public void testSettlementItemId() {
        SettlementItemId settlementItemId = new SettlementItemId("settlementThreadId", "threadId");
        
        assertEquals("settlementThreadId", settlementItemId.getSettledThreadId());
        assertEquals("threadId", settlementItemId.getThreadId());
    }
}
