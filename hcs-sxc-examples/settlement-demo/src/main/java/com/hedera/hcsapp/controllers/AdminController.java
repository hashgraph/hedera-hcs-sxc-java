package com.hedera.hcsapp.controllers;


import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.ByteString;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.proto.java.AccountID;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.hcslib.proto.java.Timestamp;

import proto.CreditBPM;
import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class AdminController {

    @Autowired
    CreditRepository creditRepository;
    
    @Autowired
    SettlementRepository settlementRepository;
    
    @Autowired
    SettlementItemRepository settlementItemRepository;
    
    @Autowired
    AddressBookRepository addressBookRepository;

    private static AppData appData;

    public AdminController() throws FileNotFoundException, IOException {

        appData = new AppData();
    }

    @GetMapping(value = "/admin/createtestdata", produces = "application/json")
    public ResponseEntity<List<Credit>> createTestData() throws FileNotFoundException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HCSLib hcsLib = appData.getHCSLib();
        LibMessagePersistence persistence = hcsLib.getMessagePersistence();

        // credits
        String threadId1 = Utils.getThreadId();
        Credit credit = new Credit();
        credit.setApplicationMessageId("0.0.1234-1111-11");
        credit.setThreadId(threadId1);
        credit.setPayerName("Bob");
        credit.setRecipientName("Alice");
        credit.setAmount(1);
        credit.setCurrency("USD");
        credit.setAdditionalNotes("memo 1");
        credit.setReference("service ref 1");
        credit.setStatus(States.CREDIT_PROPOSED_PENDING.name());
        credit.setCreatedDate("7, Nov");
        credit.setCreatedTime("10:00");
        creditRepository.save(credit);

        ApplicationMessageId applicationMessageId = ApplicationMessageId.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(1234).build())
                .setValidStart(Timestamp.newBuilder().setSeconds(1111).setNanos(11).build())
                .build();
        
        CreditBPM creditBPM = Utils.creditBPMFromCredit(credit);
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadId(threadId1)
                .setCredit(creditBPM)
                .build();
        
        ApplicationMessage applicationMessage = ApplicationMessage.newBuilder()
            .setApplicationMessageId(applicationMessageId)
            .setBusinessProcessMessage(settlementBPM.toByteString())
            .build();

        persistence.storeApplicationMessage(applicationMessageId, applicationMessage);

        ApplicationMessageChunk chunk = ApplicationMessageChunk.newBuilder()
                .setApplicationMessageId(applicationMessageId)
                .setChunkIndex(1)
                .setChunksCount(1)
                .setMessageChunk(applicationMessage.toByteString())
                .build();
        
        MirrorGetTopicMessagesResponse mirrorGetTopicMessagesResponse = MirrorGetTopicMessagesResponse.newBuilder()
                .setConsensusTimestamp(com.hedera.hashgraph.sdk.proto.Timestamp.newBuilder().setSeconds(29292929).setNanos(1111).build())
                .setRunningHash(ByteString.copyFromUtf8(""))
                .setSequenceNumber(1)
                .setMessage(chunk.toByteString())
                .build();
        
        persistence.storeMirrorResponse(mirrorGetTopicMessagesResponse);
                
        String threadId2 = Utils.getThreadId();
        credit = new Credit();
        credit.setApplicationMessageId("0.0.1234-2222-22");
        credit.setThreadId(threadId2);
        credit.setPayerName("Alice");
        credit.setRecipientName("Bob");
        credit.setAmount(2);
        credit.setCurrency("USD");
        credit.setAdditionalNotes("memo 2");
        credit.setReference("service ref 2");
        credit.setStatus(States.CREDIT_AGREED.name());
        credit.setCreatedDate("8, Nov");
        credit.setCreatedTime("11:00");
        creditRepository.save(credit);

        applicationMessageId = ApplicationMessageId.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(1234).build())
                .setValidStart(Timestamp.newBuilder().setSeconds(2222).setNanos(22).build())
                .build();
        
        creditBPM = Utils.creditBPMFromCredit(credit);
        settlementBPM = SettlementBPM.newBuilder()
                .setThreadId(threadId2)
                .setCredit(creditBPM)
                .build();
        
        applicationMessage = ApplicationMessage.newBuilder()
            .setApplicationMessageId(applicationMessageId)
            .setBusinessProcessMessage(settlementBPM.toByteString())
            .build();

        persistence.storeApplicationMessage(applicationMessageId, applicationMessage);
        
        String threadId3 = Utils.getThreadId();
        credit = new Credit();
        credit.setApplicationMessageId("0.0.1234-2222-28");
        credit.setThreadId(threadId3);
        credit.setPayerName("Carlos");
        credit.setRecipientName("Alice");
        credit.setAmount(3);
        credit.setCurrency("USD");
        credit.setAdditionalNotes("memo 3");
        credit.setReference("service ref 3");
        credit.setStatus(States.CREDIT_PROPOSED.name());
        credit.setCreatedDate("8, Nov");
        credit.setCreatedTime("11:10");
        creditRepository.save(credit);

        applicationMessageId = ApplicationMessageId.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(1234).build())
                .setValidStart(Timestamp.newBuilder().setSeconds(2222).setNanos(28).build())
                .build();
        
        creditBPM = Utils.creditBPMFromCredit(credit);
        settlementBPM = SettlementBPM.newBuilder()
                .setThreadId(threadId3)
                .setCredit(creditBPM)
                .build();
        
        applicationMessage = ApplicationMessage.newBuilder()
            .setApplicationMessageId(applicationMessageId)
            .setBusinessProcessMessage(settlementBPM.toByteString())
            .build();

        persistence.storeApplicationMessage(applicationMessageId, applicationMessage);

        
         
        String threadId4 = "1575625811-473142600";//Utils.getThreadId();
        credit = new Credit();
        credit.setApplicationMessageId("0.0.1234-2222-28");
        credit.setThreadId(threadId4);
        credit.setPayerName("Carlos");
        credit.setRecipientName("Alice");
        credit.setAmount(3);
        credit.setCurrency("USD");
        credit.setAdditionalNotes("memo 4");
        credit.setReference("service ref 4");
        credit.setStatus(States.SETTLEMENT_PROPOSED.name());
        credit.setCreatedDate("8, Nov");
        credit.setCreatedTime("11:10");
        creditRepository.save(credit);

        applicationMessageId = ApplicationMessageId.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(1234).build())
                .setValidStart(Timestamp.newBuilder().setSeconds(2222).setNanos(28).build())
                .build();
        
        creditBPM = Utils.creditBPMFromCredit(credit);
        settlementBPM = SettlementBPM.newBuilder()
                .setThreadId(threadId4)
                .setCredit(creditBPM)
                .build();
        
        applicationMessage = ApplicationMessage.newBuilder()
            .setApplicationMessageId(applicationMessageId)
            .setBusinessProcessMessage(settlementBPM.toByteString())
            .build();

        persistence.storeApplicationMessage(applicationMessageId, applicationMessage);

        
        
        
        // settlements
        String threadId = Utils.getThreadId();
        Settlement settlement = new Settlement();
        settlement.setAdditionalNotes("Settlement 1");
        settlement.setApplicationMessageId("0.0.1234-333-33");
        settlement.setCreatedDate("8, Nov");
        settlement.setCreatedTime("11:10");
        settlement.setCurrency("USD");
        settlement.setNetValue(20);
        settlement.setPayerName("Bob");
        settlement.setRecipientName("Alice");
        settlement.setStatus(States.SETTLEMENT_AGREED.name());
        settlement.setThreadId(threadId);
        settlementRepository.save(settlement);
        
        SettlementItem settlementItem = new SettlementItem();
        settlementItem.setId(new SettlementItemId(threadId1, threadId));
        settlementItem = settlementItemRepository.save(settlementItem);
        
        settlementItem = new SettlementItem();
        settlementItem.setId(new SettlementItemId(threadId2, threadId));
        settlementItem = settlementItemRepository.save(settlementItem);

        // second settlement
        threadId = Utils.getThreadId();
        settlement = new Settlement();
        settlement.setAdditionalNotes("Settlement 2");
        settlement.setApplicationMessageId("0.0.1234-4444-44");
        settlement.setCreatedDate("18, Nov");
        settlement.setCreatedTime("12:10");
        settlement.setCurrency("USD");
        settlement.setNetValue(20);
        settlement.setPayerName("Bob");
        settlement.setRecipientName("Alice");
        settlement.setStatus(States.SETTLEMENT_PROPOSED.name());
        settlement.setThreadId(threadId);
        settlementRepository.save(settlement);
        
        settlementItem = new SettlementItem();
        settlementItem.setId(new SettlementItemId(threadId3, threadId));
        settlementItem = settlementItemRepository.save(settlementItem);
       
        //third settlement
        threadId = "1575625811-473142600";
        settlement = new Settlement();
        settlement.setAdditionalNotes("Settlement 3");
        settlement.setApplicationMessageId("0.0.1234-4444-44");
        settlement.setCreatedDate("18, Nov");
        settlement.setCreatedTime("12:10");
        settlement.setCurrency("USD");
        settlement.setNetValue(20);
        settlement.setPayerName("Carlos");
        settlement.setRecipientName("Alice");
        settlement.setStatus(States.SETTLEMENT_PROPOSED.name());
        settlement.setThreadId(threadId);
        settlementRepository.save(settlement);
        
        settlementItem = new SettlementItem();
        settlementItem.setId(new SettlementItemId(threadId3, threadId));
        settlementItem = settlementItemRepository.save(settlementItem);
        
      
        return new ResponseEntity<>(headers, HttpStatus.OK);
        
       
        
        
    }

    @GetMapping(value = "/admin/deletedata", produces = "application/json")
    public ResponseEntity<List<Credit>> deleteData() throws FileNotFoundException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HCSLib hcsLib = appData.getHCSLib();
        LibMessagePersistence persistence = hcsLib.getMessagePersistence();

        persistence.clear();
        
        creditRepository.deleteAll();
        settlementRepository.deleteAll();
        settlementItemRepository.deleteAll();
        
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
}
