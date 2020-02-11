package com.hedera.hcsapp;

import org.junit.jupiter.api.Test;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;
import com.hedera.hcs.sxc.proto.Timestamp;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.restclasses.SettlementRest;

import proto.CreditBPM;
import proto.Money;
import proto.SettleProposeBPM;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Locale;

public class UtilsTest {    
    
    @Test
    public void testTimestampToDate() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        String formattedDate = Utils.timestampToDate(1580914593, 0);
        assertEquals("5 Feb", formattedDate);
    }

    @Test
    public void testTimestampToTime() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        String formattedTime = Utils.timestampToTime(1580914593, 0);
        assertEquals("14:56", formattedTime);
    }

    @Test
    public void testApplicationMessageIdToString() {
        ApplicationMessageID.Builder applicationMessageID = ApplicationMessageID.newBuilder();
        applicationMessageID.setAccountID(AccountID.newBuilder().setAccountNum(2));
        applicationMessageID.setValidStart(Timestamp.newBuilder().setSeconds(10).setNanos(20));
        String appliString = Utils.applicationMessageIdToString(applicationMessageID.build());
        
        assertEquals("0.0.2-10-20", appliString);
    }

    @Test
    public void testTransactionIdToString() {
        Instant timeNow = Instant.now();
        String txCompare = "0.0.2-".concat(Long.toString(timeNow.getEpochSecond())).concat("-").concat(Integer.toString(timeNow.getNano()));
        
        TransactionId transactionId = TransactionId.withValidStart(AccountId.fromString("0.0.2"), timeNow);
        
        String txString = Utils.transactionIdToString(transactionId);
        
        assertEquals(txCompare, txString);
    }

    @Test
    public void testCreditFromCreditBPM() {
        CreditBPM.Builder creditBPM = CreditBPM.newBuilder();
        
        creditBPM.setAdditionalNotes("additional notes");
        creditBPM.setApplicationMessageID("application message id");
        creditBPM.setCreatedDate("created date");
        creditBPM.setCreatedTime("created time");
        creditBPM.setPayerName("Payer name");
        creditBPM.setRecipientName("Recipient name");
        creditBPM.setServiceRef("Service ref");
        creditBPM.setValue(Money.newBuilder().setCurrencyCode("currency").setUnits(10).setNanos(20).build());
        
        Credit credit = Utils.creditFromCreditBPM(creditBPM.build(), "threadId");
        
        assertEquals(creditBPM.getAdditionalNotes(), credit.getAdditionalNotes());
        assertEquals(creditBPM.getApplicationMessageID(), credit.getApplicationMessageId());
        assertEquals(creditBPM.getCreatedDate(), credit.getCreatedDate());
        assertEquals(creditBPM.getCreatedTime(), credit.getCreatedTime());
        assertEquals(creditBPM.getPayerName(), credit.getPayerName());
        assertEquals(creditBPM.getRecipientName(), credit.getRecipientName());
        assertEquals(creditBPM.getServiceRef(), credit.getReference());
        assertEquals(creditBPM.getValue().getUnits(), credit.getAmount());
        assertEquals(creditBPM.getValue().getCurrencyCode(), credit.getCurrency());
        assertEquals("threadId", credit.getThreadId());
        assertNull(credit.getStatus());
    }

    @Test
    public void testCreditBPMFromCredit() {

        Credit credit = new Credit();
        
        credit.setAdditionalNotes("additional notes");
        credit.setApplicationMessageId("application message id");
        credit.setCreatedDate("created date");
        credit.setCreatedTime("created time");
        credit.setPayerName("Payer name");
        credit.setRecipientName("Recipient name");
        credit.setReference("Service ref");
        credit.setAmount(10);
        credit.setCurrency("currency");
        credit.setThreadId("threadid");
        
        CreditBPM creditBPM = Utils.creditBPMFromCredit(credit);
        
        assertEquals(credit.getAdditionalNotes(), creditBPM.getAdditionalNotes());
        assertEquals(credit.getApplicationMessageId(), creditBPM.getApplicationMessageID());
        assertEquals(credit.getCreatedDate(), creditBPM.getCreatedDate());
        assertEquals(credit.getCreatedTime(), creditBPM.getCreatedTime());
        assertEquals(credit.getPayerName(), creditBPM.getPayerName());
        assertEquals(credit.getRecipientName(), creditBPM.getRecipientName());
        assertEquals(credit.getReference(), creditBPM.getServiceRef());
        assertEquals(credit.getAmount(), creditBPM.getValue().getUnits());
        assertEquals(credit.getCurrency(), creditBPM.getValue().getCurrencyCode());

    }

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
        
        Settlement settle = Utils.settlementFromSettleProposeBPM(settleBPM.build(), "threadId");
        
        assertEquals(settleBPM.getAdditionalNotes(), settle.getAdditionalNotes());
        assertEquals(settleBPM.getCreatedDate(), settle.getCreatedDate());
        assertEquals(settleBPM.getCreatedTime(), settle.getCreatedTime());
        assertEquals(settleBPM.getNetValue().getUnits(), settle.getNetValue());
        assertEquals(settleBPM.getPayerName(), settle.getPayerName());
        assertEquals(settleBPM.getRecipientName(), settle.getRecipientName());
        assertEquals("threadId", settle.getThreadId());
    }

    @Test
    public void testApplicationMessageIdFromString() {
        String appString = "0.1.2-10-20";
        ApplicationMessageID applicationMessageID = Utils.applicationMessageIdFromString(appString);
        
        assertEquals(0, applicationMessageID.getAccountID().getShardNum());
        assertEquals(1, applicationMessageID.getAccountID().getRealmNum());
        assertEquals(2, applicationMessageID.getAccountID().getAccountNum());
        assertEquals(10, applicationMessageID.getValidStart().getSeconds());
        assertEquals(20,  applicationMessageID.getValidStart().getNanos());
    }

    @Test
    public void testGetThreadId() {
        String threadId = Utils.getThreadId();
        assertNotNull(threadId);
    }

    @Test
    public void testMoneyFromSettlement() {
        Settlement settlement = new Settlement();
        settlement.setCurrency("currency");
        settlement.setNetValue(10);
        Money money = Utils.moneyFromSettlement(settlement);
        assertEquals("currency", money.getCurrencyCode());
        assertEquals(10, money.getUnits());
        assertEquals(0, money.getNanos());
    }

    @Test
    public void testResponseEntity() {
        ResponseEntity<SettlementRest> response = Utils.serverError();
        assertEquals("application/json", response.getHeaders().get("Content-Type").get(0));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
