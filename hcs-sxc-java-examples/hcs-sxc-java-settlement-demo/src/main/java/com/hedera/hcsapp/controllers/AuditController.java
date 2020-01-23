package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
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
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageId;

import lombok.extern.log4j.Log4j2;
import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Log4j2
@RestController
public class AuditController {

    @Autowired
    CreditRepository creditRepository;

    @Autowired
    SettlementRepository settlementRepository;

    private static AppData appData;

    public AuditController() throws Exception {

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
                    , States.valueOf(credit.getStatus()).getDisplayForCredit()
                    , appData.getHCSCore().getTopicIds().get(appData.getTopicIndex()).toString()
                    , credit.getCreatedDate()
                    , credit.getCreatedTime()
                )
            );
        }

        for (Settlement settlement : settlementRepository.findAll()) {
            threads.put(settlement.getThreadId(), new AuditThreadId (
                    settlement.getThreadId()
                    , "Settlement"
                    , States.valueOf(settlement.getStatus()).getDisplayForSettlement()
                    , appData.getHCSCore().getTopicIds().get(appData.getTopicIndex()).toString()
                    , settlement.getCreatedDate()
                    , settlement.getCreatedTime()
                )
            );
        }

        AuditThreadIds auditThreadIds = new AuditThreadIds();

        // sort the list
        threads.entrySet()
            .stream()
            .sorted(Map.Entry.<String, AuditThreadId>comparingByKey(Comparator.reverseOrder()))
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
        HCSCore hcsCore = appData.getHCSCore();
        SxcMessagePersistence persistence = hcsCore.getMessagePersistence();

        Map<String, ApplicationMessage> applicationMessages = persistence.getApplicationMessages();

        SortedSet<String> applicationMessageIds = new TreeSet<>(applicationMessages.keySet());
        for (String applicationMessageId : applicationMessageIds) {
           SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessages.get(applicationMessageId).getBusinessProcessMessage());

           if (settlementBPM.getThreadId().equals(threadId)) {
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
        HCSCore hcsCore = appData.getHCSCore();
        SxcMessagePersistence persistence = hcsCore.getMessagePersistence();

        Map<String, SxcConsensusMessage> mirrorResponses = persistence.getMirrorResponses();

        for (Map.Entry<String, SxcConsensusMessage> mirrorResponse : mirrorResponses.entrySet()) {

            try {
                ApplicationMessageChunk chunk = ApplicationMessageChunk.parseFrom(mirrorResponse.getValue().message);

                ApplicationMessageId applicationMessageIdProto = chunk.getApplicationMessageId();

                String appMessageId = applicationMessageIdProto.getAccountID().getShardNum()
                        + "." + applicationMessageIdProto.getAccountID().getRealmNum()
                        + "." + applicationMessageIdProto.getAccountID().getAccountNum()
                        + "-" + applicationMessageIdProto.getValidStart().getSeconds()
                        + "-" + applicationMessageIdProto.getValidStart().getNanos();

                if (appMessageId.contentEquals(applicationMessageId)) {
                    AuditHCSMessage auditHCSMessage = new AuditHCSMessage(appData);
                    auditHCSMessage.setConsensusTimeStampSeconds(mirrorResponse.getValue().consensusTimestamp.getEpochSecond());
                    auditHCSMessage.setConsensusTimeStampNanos(mirrorResponse.getValue().consensusTimestamp.getNano());
                    byte[] runningHash = mirrorResponse.getValue().runningHash;
                    auditHCSMessage.setRunningHash(Hex.encodeHexString(runningHash));
                    auditHCSMessage.setSequenceNumber(mirrorResponse.getValue().sequenceNumber);

                    auditHCSMessage.setPart(chunk.getChunkIndex() + " of " + chunk.getChunksCount());

                    auditHCSMessage.setMessage(ApplicationMessage.parseFrom(chunk.getMessageChunk()).toString());

                    auditHCSMessages.getAuditHCSMessages().add(auditHCSMessage);
                }
            } catch (InvalidProtocolBufferException e) {
                log.error(e);
            }
        }

        return new ResponseEntity<>(auditHCSMessages, headers, HttpStatus.OK);
    }

}
