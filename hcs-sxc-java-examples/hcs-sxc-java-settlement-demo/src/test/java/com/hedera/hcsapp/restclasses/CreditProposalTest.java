package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CreditProposalTest {
    
    @Test
    public void testCreditProposal() {
        CreditProposal creditProposal = new CreditProposal();
        
        assertFalse(creditProposal.isAutomatic());
        
        creditProposal.setAdditionalNotes("additionalNotes");
        creditProposal.setAmount(10);
        creditProposal.setAutomatic(true);
        creditProposal.setCurrency("currency");
        creditProposal.setPayerName("payerName");
        creditProposal.setRecipientName("recipientName");
        creditProposal.setReference("reference");
        
        assertEquals("additionalNotes", creditProposal.getAdditionalNotes());
        assertEquals(10, creditProposal.getAmount());
        assertTrue(creditProposal.isAutomatic());
        assertEquals("currency", creditProposal.getCurrency());
        assertEquals("payerName", creditProposal.getPayerName());
        assertEquals("recipientName", creditProposal.getRecipientName());
        assertEquals("reference", creditProposal.getReference());
    }
}
