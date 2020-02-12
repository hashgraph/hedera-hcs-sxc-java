package com.hedera.hcsapp.controllers;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */
import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.config.Topic;
import com.hedera.hcs.sxc.interfaces.SxcKeyRotation;
import com.hedera.hcs.sxc.interfaces.SxcMessageEncryption;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.plugins.Plugins;
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
import com.hedera.hcs.sxc.proto.ApplicationMessageID;

import lombok.extern.log4j.Log4j2;
import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    private AppData appData;
    //private HCSCore hCSCore;
    private boolean signMessages;
    private boolean encryptMessages;
    private boolean rotateKeys;
    private List<Topic> topics;
    private SxcMessageEncryption messageEncryptionPlugin;
    private SxcKeyRotation keyRotationPlugin;
    public AuditController() throws Exception {
        appData = new AppData();
         HCSCore hcsCore = appData.getHCSCore();
     
          
        this.signMessages = hcsCore.getSignMessages();
        this.encryptMessages = hcsCore.getEncryptMessages();
        this.rotateKeys = hcsCore.getRotateKeys();
        this.topics = hcsCore.getTopics();
        
        if(this.signMessages){
            
        }
        if (this.encryptMessages){
            Class<?> messageEncryptionClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcMessageEncryption", true);
            this.messageEncryptionPlugin = (SxcMessageEncryption)messageEncryptionClass.newInstance();
        }
         if(this.rotateKeys){
            Class<?> messageKeyRotationClass = Plugins.find("com.hedera.hcs.sxc.plugin.cryptography.*", "com.hedera.hcs.sxc.interfaces.SxcKeyRotation", true);
            this.keyRotationPlugin = (SxcKeyRotation)messageKeyRotationClass.newInstance();
        }
    }

    @GetMapping(value = "/audit", produces = "application/json")
    public ResponseEntity<AuditThreadIds> threadIds() throws FileNotFoundException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, AuditThreadId> threads = new HashMap<>();

        for (Credit credit : creditRepository.findAll()) {
            threads.put(credit.getThreadId(), new AuditThreadId(
                    credit.getThreadId(),
                     "Credit",
                     States.valueOf(credit.getStatus()).getDisplayForCredit(),
                     appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic(),
                     credit.getCreatedDate(),
                     credit.getCreatedTime()
            )
            );
        }

        for (Settlement settlement : settlementRepository.findAll()) {
            threads.put(settlement.getThreadId(), new AuditThreadId(
                    settlement.getThreadId(),
                     "Settlement",
                     States.valueOf(settlement.getStatus()).getDisplayForSettlement(),
                     appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic(),
                     settlement.getCreatedDate(),
                     settlement.getCreatedTime()
            )
            );
        }

        AuditThreadIds auditThreadIds = new AuditThreadIds();

        // sort the list
        threads.entrySet()
                .stream()
                .sorted(Map.Entry.<String, AuditThreadId>comparingByKey(Comparator.reverseOrder()))
                .forEach((auditThreadId) -> {
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
        SxcPersistence persistence = hcsCore.getMessagePersistence();

        Map<String, ApplicationMessage> applicationMessages = persistence.getApplicationMessages();

        SortedSet<String> applicationMessageIds = new TreeSet<>(applicationMessages.keySet());
        for (String applicationMessageId : applicationMessageIds) {
            //SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessages.get(applicationMessageId).getBusinessProcessMessage());

            if (!appData.getHCSCore().getEncryptMessages()) {

                SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessages.get(applicationMessageId).getBusinessProcessMessage());
                if (settlementBPM.getThreadID().equals(threadId)) {
                    AuditApplicationMessage auditApplicationMessage = new AuditApplicationMessage(appData);
                    auditApplicationMessage.setApplicationMessageId(applicationMessageId);
                    auditApplicationMessage.setMessage(settlementBPM.toString());
                    auditApplicationMessages.getAuditApplicationMessages().add(auditApplicationMessage);
                }
                
            } else {

                AuditApplicationMessage auditApplicationMessage = new AuditApplicationMessage(appData);
                auditApplicationMessage.setApplicationMessageId(applicationMessageId);
                auditApplicationMessage.setMessage("Business Process Message ENCRYPTED");
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
        SxcPersistence persistence = hcsCore.getMessagePersistence();

        Map<String, SxcConsensusMessage> mirrorResponses = persistence.getMirrorResponses();

        for (Map.Entry<String, SxcConsensusMessage> mirrorResponse : mirrorResponses.entrySet()) {

            try {
                ApplicationMessageChunk chunk = ApplicationMessageChunk.parseFrom(mirrorResponse.getValue().message);

                ApplicationMessageID applicationMessageIdProto = chunk.getApplicationMessageId();

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
