package com.hedera.hcsapp.restclasses;

import org.junit.jupiter.api.Test;

import com.hedera.hcsapp.restclasses.SettlementRestTest;

import proto.Money;
import proto.SettleProposeBPM;

public class SettlementRestTest {    
    
    @Test
    public void testSettlementFromSettleProposeBPM() {
        SettleProposeBPM.Builder settleBPM = SettleProposeBPM.newBuilder();
        
        settleBPM.setAdditionalNotes("additional notes");
        settleBPM.setCreatedDate("created date");
        settleBPM.setCreatedTime("created time");
        settleBPM.setNetValue(Money.newBuilder().setCurrencyCode("currency").setUnits(10).setNanos(20).build());
        settleBPM.setPayerName("Payer name");
        settleBPM.setRecipientName("Recipient name");
        settleBPM.addThreadIDs("thread1");
        settleBPM.addThreadIDs("thread2");
//        
//        Settlement settle = Utils.settlementFromSettleProposeBPM(settleBPM.build(), "threadId");
//        
//        assertEquals(settleBPM.getAdditionalNotes(), settle.getAdditionalNotes());
//        assertEquals(settleBPM.getCreatedDate(), settle.getCreatedDate());
//        assertEquals(settleBPM.getCreatedTime(), settle.getCreatedTime());
//        assertEquals(settleBPM.getNetValue().getUnits(), settle.getNetValue());
//        assertEquals(settleBPM.getPayerName(), settle.getPayerName());
//        assertEquals(settleBPM.getRecipientName(), settle.getRecipientName());
//        assertEquals("threadId", settle.getThreadId());
    }

}
