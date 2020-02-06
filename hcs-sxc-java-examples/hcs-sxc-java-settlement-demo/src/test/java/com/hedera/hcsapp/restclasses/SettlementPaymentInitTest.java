package com.hedera.hcsapp.restclasses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SettlementPaymentInitTest {
    
    @Test
    public void testSettlementPaymentInit() {
        SettlementPaymentInit settlementPaymentInit = new SettlementPaymentInit();
        
        settlementPaymentInit.setAdditionalNotes("additionalNotes");
        settlementPaymentInit.setThreadId("threadId");
        settlementPaymentInit.setPayerAccountDetails("payerAccountDetails");
        settlementPaymentInit.setRecipientAccountDetails("recipientAccountDetails");
        
        assertEquals("additionalNotes", settlementPaymentInit.getAdditionalNotes());
        assertEquals("threadId", settlementPaymentInit.getThreadId());
        assertEquals("payerAccountDetails", settlementPaymentInit.getPayerAccountDetails());
        assertEquals("recipientAccountDetails", settlementPaymentInit.getRecipientAccountDetails());
    }
}
