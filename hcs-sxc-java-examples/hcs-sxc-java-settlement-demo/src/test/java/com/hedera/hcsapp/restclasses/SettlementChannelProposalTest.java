package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SettlementChannelProposalTest {
    
    @Test
    public void testSettlementChannelProposal() {
        SettlementChannelProposal settlementChannelProposal = new SettlementChannelProposal();
        
        settlementChannelProposal.setAdditionalNotes("additionalNotes");
        settlementChannelProposal.setPaymentChannelName("paymentChannelName");
        settlementChannelProposal.setThreadId("threadId");
        
        assertEquals("additionalNotes", settlementChannelProposal.getAdditionalNotes());
        assertEquals("paymentChannelName", settlementChannelProposal.getPaymentChannelName());
        assertEquals("threadId", settlementChannelProposal.getThreadId());
    }
}
