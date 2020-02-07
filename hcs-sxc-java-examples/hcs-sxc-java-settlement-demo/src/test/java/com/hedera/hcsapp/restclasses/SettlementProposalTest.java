package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class SettlementProposalTest {
    
    @Test
    public void testSettlementProposal() {
        SettlementProposal settlementProposal = new SettlementProposal();
        
        settlementProposal.setAdditionalNotes("additionalNotes");
        settlementProposal.setAutomatic(true);
        settlementProposal.setCurrency("currency");
        settlementProposal.setNetValue(100);
        settlementProposal.setPayerName("payerName");
        settlementProposal.setRecipientName("recipientName");
        
        List<String> threadIds = new ArrayList<String>();
        threadIds.add("thread1");
        threadIds.add("thread2");
        
        settlementProposal.setThreadIds(threadIds);
        
        assertEquals("additionalNotes", settlementProposal.getAdditionalNotes());
        assertTrue(settlementProposal.isAutomatic());
        assertEquals("currency", settlementProposal.getCurrency());
        assertEquals(100, settlementProposal.getNetValue());
        assertEquals("payerName", settlementProposal.getPayerName());
        assertEquals("recipientName", settlementProposal.getRecipientName());
        assertEquals(2, settlementProposal.getThreadIds().size());
        assertEquals("thread1", settlementProposal.getThreadIds().get(0));
        assertEquals("thread2", settlementProposal.getThreadIds().get(1));
    }
}
