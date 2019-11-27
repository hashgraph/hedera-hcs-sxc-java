package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.restclasses.AuditApplicationMessage;
import com.hedera.hcsapp.restclasses.AuditApplicationMessages;
import com.hedera.hcsapp.restclasses.AuditHCSMessage;
import com.hedera.hcsapp.restclasses.AuditHCSMessages;
import com.hedera.hcsapp.restclasses.AuditThreadId;
import com.hedera.hcsapp.restclasses.AuditThreadIds;
import com.hedera.hcsapp.restclasses.CreditProposal;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.consensus.OutboundHCSMessage;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

import lombok.extern.log4j.Log4j2;
import proto.CreditAckBPM;
import proto.CreditBPM;
import proto.Money;
import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@RestController
public class AuditController {

    @Autowired
    CreditRepository creditRepository;

    @Autowired
    SettlementRepository settlementRepository;

    private static AppData appData;
//    private static int topicIndex = 0; // refers to the first topic ID in the config.yaml

    public AuditController() throws FileNotFoundException, IOException {

        appData = new AppData();
    }

    @GetMapping(value = "/audit", produces = "application/json")
    public ResponseEntity<AuditThreadIds> threadIds() throws FileNotFoundException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, AuditThreadId> threads = new HashMap<>();
        
        for (Credit credit : creditRepository.findAll()) {
            threads.put(credit.getThreadId(), new AuditThreadId(credit.getThreadId(), "Credit"));
        }
        
        for (Settlement settlement : settlementRepository.findAll()) {
            threads.put(settlement.getThreadId(), new AuditThreadId(settlement.getThreadId(), "Settlement"));
        }

        AuditThreadIds auditThreadIds = new AuditThreadIds();

        // sort the list
        threads.entrySet()
            .stream()
            .sorted(Map.Entry.<String, AuditThreadId>comparingByKey())
            .forEach( (auditThreadId) -> {
                auditThreadIds.getThreadIds().add(auditThreadId.getValue());
            }
        );
        
//        for (Map.Entry<String, AuditThreadId> auditThread : threads.entrySet()) {
//            auditThreadIds.getThreadIds().add(auditThread.getValue());
//        }
        
        return new ResponseEntity<>(auditThreadIds, headers, HttpStatus.OK);
    }
    
    @GetMapping(value = "/audit/{threadId}", produces = "application/json")
    public ResponseEntity<AuditApplicationMessages> applicationMessages(@PathVariable String threadId) throws InvalidProtocolBufferException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        AuditApplicationMessages auditApplicationMessages = new AuditApplicationMessages();
        HCSLib hcsLib = appData.getHCSLib();
        LibMessagePersistence persistence = hcsLib.getMessagePersistence();
        
        
        Map<String, ApplicationMessage> applicationMessages = persistence.getApplicationMessages();
        
        for (Map.Entry<String, ApplicationMessage> applicationMessage : applicationMessages.entrySet()) {
            String applicationMessageId = applicationMessage.getKey();
            SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessage.getValue().getBusinessProcessMessage());
            
            if (settlementBPM.getThreadId().contentEquals(threadId)) {
                AuditApplicationMessage auditApplicationMessage = new AuditApplicationMessage();
                auditApplicationMessage.setApplicationMessageId(applicationMessageId);
                auditApplicationMessage.setMessage(settlementBPM.toString());
                auditApplicationMessages.getAuditApplicationMessages().add(auditApplicationMessage);
            }            
            
        }

        return new ResponseEntity<>(auditApplicationMessages, headers, HttpStatus.OK);
    }
    
    @GetMapping(value = "/audit/{threadId}/{applicationMessageId}", produces = "application/json")
    public ResponseEntity<AuditHCSMessages> applicationMessages(@PathVariable String threadId, @PathVariable String applicationMessageId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        AuditHCSMessages auditHCSMessages = new AuditHCSMessages();
        HCSLib hcsLib = appData.getHCSLib();
        LibMessagePersistence persistence = hcsLib.getMessagePersistence();

        Map<String, MirrorGetTopicMessagesResponse> mirrorResponses = persistence.getMirrorResponses();

        for (Map.Entry<String, MirrorGetTopicMessagesResponse> mirrorResponse : mirrorResponses.entrySet()) {
            
            try {
                ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(mirrorResponse.getValue().getMessage());
                ApplicationMessageId applicationMessageIdProto = applicationMessage.getApplicationMessageId();
                String appMessageId = applicationMessageIdProto.getAccountID().getShardNum()
                        + "." + applicationMessageIdProto.getAccountID().getRealmNum()
                        + "." + applicationMessageIdProto.getAccountID().getAccountNum()
                        + "-" + applicationMessageIdProto.getValidStart().getSeconds()
                        + "-" + applicationMessageIdProto.getValidStart().getNanos();
                
                if (appMessageId.contentEquals(applicationMessageId)) {
                    AuditHCSMessage auditHCSMessage = new AuditHCSMessage();
                    auditHCSMessage.setConsensusTimeStampSeconds(mirrorResponse.getValue().getConsensusTimestamp().getSeconds());
                    auditHCSMessage.setConsensusTimeStampNanos(mirrorResponse.getValue().getConsensusTimestamp().getNanos());
                    auditHCSMessage.setRunningHash(mirrorResponse.getValue().getRunningHash().toStringUtf8());
                    auditHCSMessage.setSequenceNumber(mirrorResponse.getValue().getSequenceNumber());
                    auditHCSMessage.setMessage(applicationMessage.toString());
                    
                    auditHCSMessages.getAuditHCSMessages().add(auditHCSMessage);
                }
            } catch (InvalidProtocolBufferException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return new ResponseEntity<>(auditHCSMessages, headers, HttpStatus.OK);
    }

}
