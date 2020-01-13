package com.hedera.hcsapp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcs.sxc.proto.java.AccountID;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageId;
import com.hedera.hcs.sxc.proto.java.Timestamp;

import proto.CreditBPM;
import proto.Money;
import proto.SettleProposeBPM;

public final class Utils {
    private static Random random = new Random();

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
    
    public static Credit creditFromCreditBPM(CreditBPM creditBPM, String threadId) {
        Credit credit = new Credit();
        
        credit.setAdditionalNotes(creditBPM.getAdditionalNotes());
        credit.setPayerName(creditBPM.getPayerName());
        credit.setRecipientName(creditBPM.getRecipientName());
        credit.setReference(creditBPM.getServiceRef());
        credit.setApplicationMessageId(creditBPM.getApplicationMessageId());
        credit.setCreatedDate(creditBPM.getCreatedDate());
        credit.setCreatedTime(creditBPM.getCreatedTime());
        credit.setAmount(creditBPM.getValue().getUnits());
        credit.setCurrency(creditBPM.getValue().getCurrencyCode());
        credit.setThreadId(threadId);
        
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
                .setApplicationMessageId(credit.getApplicationMessageId())
                .setCreatedDate(credit.getCreatedDate())
                .setCreatedTime(credit.getCreatedTime())
                .setValue(value)
                .build();

        return creditBPM;
    }

    public static Settlement settlementFromSettleProposeBPM(SettleProposeBPM settleProposeBPM, String threadId) {
        Settlement settlement = new Settlement();
        
        settlement.setAdditionalNotes(settleProposeBPM.getAdditionalNotes());
        settlement.setCurrency(settleProposeBPM.getNetValue().getCurrencyCode());
        settlement.setNetValue(settleProposeBPM.getNetValue().getUnits());
        settlement.setPayerName(settleProposeBPM.getPayerName());
        settlement.setRecipientName(settleProposeBPM.getRecipientName());
        settlement.setThreadId(threadId);
        settlement.setCreatedDate(settleProposeBPM.getCreatedDate());
        settlement.setCreatedTime(settleProposeBPM.getCreatedTime());
        
        return settlement;
    }
    public static ApplicationMessageId applicationMessageIdFromString(String appMessageId) {
        String[] messageIdParts = appMessageId.split("-");
        String[] account = messageIdParts[0].split("\\.");
        
        AccountID accountId = AccountID.newBuilder()
                .setShardNum(Long.parseLong(account[0]))
                .setRealmNum(Long.parseLong(account[1]))
                .setAccountNum(Long.parseLong(account[2]))
                .build();
        
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(Long.parseLong(messageIdParts[1]))
                .setNanos(Integer.parseInt(messageIdParts[2]))
                .build();
        
        ApplicationMessageId applicationMessageId = ApplicationMessageId.newBuilder()
                .setAccountID(accountId)
                .setValidStart(timestamp)
                .build();
        
        return applicationMessageId;
    }
    
    public static String getThreadId() {
        Instant now = Instant.now();
        long nano = now.getNano();
        
        long remainder = nano - (nano / 1000 * 1000); // check nanos end with 000.
        if (remainder == 0) {
            int rndNano = random.nextInt(1000);
            nano = nano + rndNano;
        }
     
        return now.getEpochSecond() + "-" + nano;
    }
}
