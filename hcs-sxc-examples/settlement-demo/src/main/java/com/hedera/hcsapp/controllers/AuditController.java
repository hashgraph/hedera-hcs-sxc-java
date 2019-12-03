package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.restclasses.AuditApplicationMessage;
import com.hedera.hcsapp.restclasses.AuditApplicationMessages;
import com.hedera.hcsapp.restclasses.AuditHCSMessage;
import com.hedera.hcsapp.restclasses.AuditHCSMessages;
import com.hedera.hcsapp.restclasses.AuditThreadId;
import com.hedera.hcsapp.restclasses.AuditThreadIds;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class AuditController {

    @Autowired
    CreditRepository creditRepository;

    @Autowired
    SettlementRepository settlementRepository;

    private static AppData appData;

    public AuditController() throws FileNotFoundException, IOException {

        appData = new AppData();
    }

    @GetMapping(value = "/audit", produces = "application/json")
    public ResponseEntity<AuditThreadIds> threadIds() throws FileNotFoundException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, AuditThreadId> threads = new HashMap<>();
        
        for (Credit credit : creditRepository.findAll()) {
            threads.put(credit.getThreadId(), new AuditThreadId (
                    credit.getThreadId()
                    , "Credit"
                    , credit.getStatus()
                    , appData.getHCSLib().getTopicIds().get(appData.getTopicIndex()).toString()
                    , credit.getCreatedDate()
                    , credit.getCreatedTime()
                )
            );
        }
        
        for (Settlement settlement : settlementRepository.findAll()) {
            threads.put(settlement.getThreadId(), new AuditThreadId (
                    settlement.getThreadId()
                    , "Settlement"
                    , settlement.getStatus()
                    , appData.getHCSLib().getTopicIds().get(appData.getTopicIndex()).toString()
                    , settlement.getCreatedDate()
                    , settlement.getCreatedTime()
                )
            );
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
                AuditApplicationMessage auditApplicationMessage = new AuditApplicationMessage(appData);
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
                ApplicationMessageChunk chunk = ApplicationMessageChunk.parseFrom(mirrorResponse.getValue().getMessage());

                ApplicationMessageId applicationMessageIdProto = chunk.getApplicationMessageId();

                String appMessageId = applicationMessageIdProto.getAccountID().getShardNum()
                        + "." + applicationMessageIdProto.getAccountID().getRealmNum()
                        + "." + applicationMessageIdProto.getAccountID().getAccountNum()
                        + "-" + applicationMessageIdProto.getValidStart().getSeconds()
                        + "-" + applicationMessageIdProto.getValidStart().getNanos();
                
                if (appMessageId.contentEquals(applicationMessageId)) {
                    AuditHCSMessage auditHCSMessage = new AuditHCSMessage(appData);
                    auditHCSMessage.setConsensusTimeStampSeconds(mirrorResponse.getValue().getConsensusTimestamp().getSeconds());
                    auditHCSMessage.setConsensusTimeStampNanos(mirrorResponse.getValue().getConsensusTimestamp().getNanos());
                    auditHCSMessage.setRunningHash(mirrorResponse.getValue().getRunningHash().toStringUtf8());
                    auditHCSMessage.setSequenceNumber(mirrorResponse.getValue().getSequenceNumber());
                    
                    auditHCSMessage.setPart(chunk.getChunkIndex() + " of " + chunk.getChunksCount());
                    
                    auditHCSMessage.setMessage(ApplicationMessage.parseFrom(chunk.getMessageChunk()).toString());
                    
                    auditHCSMessages.getAuditHCSMessages().add(auditHCSMessage);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        return new ResponseEntity<>(auditHCSMessages, headers, HttpStatus.OK);
    }

}
