package com.hedera.hcsapp;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcslib.proto.java.ApplicationMessageId;

import proto.CreditBPM;
import proto.Money;
import proto.SettleProposeBPM;

public final class Utils {
    public static String TimestampToDate(long seconds, int nanos) {
        
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault());
        String formattedDate = dateTime.format(formatter);
        return formattedDate;
        
    }
    
    public static String TimestampToTime(long seconds, int nanos) {
        
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault());
        String formattedDate = dateTime.format(formatter);
        return formattedDate;
    }
    
    public static String TransactionIdToString(ApplicationMessageId transactionId) {
        String txId = "0.0." + transactionId.getAccountID().getAccountNum()
                + "-" + transactionId.getValidStart().getSeconds()
                + "-" + transactionId.getValidStart().getNanos();
        return txId;
    }
    
    public static String TransactionIdToString(TransactionId transactionId) {
        String txId = "0.0." + transactionId.getAccountId().getAccountNum()
                + "-" + transactionId.getValidStart().getEpochSecond()
                + "-" + transactionId.getValidStart().getNano();
        return txId;
    }
    
    public static Credit creditFromCreditBPM(CreditBPM creditBPM) {
        Credit credit = new Credit();
        
        credit.setAdditionalNotes(creditBPM.getAdditionalNotes());
        credit.setPayerName(creditBPM.getPayerName());
        credit.setRecipientName(creditBPM.getRecipientName());
        credit.setReference(creditBPM.getServiceRef());
        credit.setTransactionId(creditBPM.getTransactionId());
        credit.setCreatedDate(creditBPM.getCreatedDate());
        credit.setCreatedTime(creditBPM.getCreatedTime());
        credit.setAmount(creditBPM.getValue().getUnits());
        credit.setCurrency(creditBPM.getValue().getCurrencyCode());
        credit.setThreadId(creditBPM.getThreadId());
        
        return credit;
    }
    
    public static CreditBPM creditBPMFromCredit(Credit credit) {
        Money value = Money.newBuilder()
                .setCurrencyCode(credit.getCurrency())
                .setUnits(credit.getAmount())
                .build();

        CreditBPM creditBPM = CreditBPM.newBuilder()
                .setAdditionalNotes(credit.getAdditionalNotes())
                .setPayerName(credit.getPayerName())
                .setRecipientName(credit.getRecipientName())
                .setServiceRef(credit.getReference())
                .setTransactionId(credit.getTransactionId())
                .setCreatedDate(credit.getCreatedDate())
                .setCreatedTime(credit.getCreatedTime())
                .setValue(value)
                .setThreadId(credit.getThreadId())
                .build();

        return creditBPM;
    }

    public static Settlement settlementFromSettleProposeBPM(SettleProposeBPM settleProposeBPM) {
        Settlement settlement = new Settlement();
        
        settlement.setAdditionalNotes(settleProposeBPM.getAdditionalNotes());
        settlement.setCurrency(settleProposeBPM.getNetValue().getCurrencyCode());
        settlement.setNetValue(settleProposeBPM.getNetValue().getUnits());
        settlement.setPayerName(settleProposeBPM.getPayerName());
        settlement.setRecipientName(settleProposeBPM.getRecipientName());
        settlement.setThreadId(settleProposeBPM.getThreadId());
        
        return settlement;
    }

}
