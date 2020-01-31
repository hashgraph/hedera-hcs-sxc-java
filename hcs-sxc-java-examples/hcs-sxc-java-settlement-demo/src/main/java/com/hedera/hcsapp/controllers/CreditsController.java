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

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.restclasses.CreditProposal;
import com.hedera.hcsapp.restclasses.CreditRest;

import lombok.extern.log4j.Log4j2;
import proto.CreditAckBPM;
import proto.CreditBPM;
import proto.Money;
import proto.SettlementBPM;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
public class CreditsController {

    @Autowired
    CreditRepository creditRepository;
    @Autowired
    AddressBookRepository addressBookRepository;

    private static AppData appData;

    public CreditsController() throws Exception {

        appData = new AppData();
    }

    @GetMapping(value = "/credits/{user}", produces = "application/json")
    public ResponseEntity<List<CreditRest>> credits(@PathVariable String user) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        AppData appData = new AppData();
        List<Credit> creditList = new ArrayList<Credit>();
        List<CreditRest> restResponse = new ArrayList<CreditRest>();

        if (user == null) {
            creditList = (List<Credit>) creditRepository.findAllDesc();
        } else {
            creditList = creditRepository.findAllCreditsForUsers(appData.getUserName(), user);
        }
        
        for (Credit credit : creditList) {
            restResponse.add(new CreditRest(credit, appData));
        }

        return new ResponseEntity<>(restResponse, headers, HttpStatus.OK);
    }

//    @Transactional
    @PostMapping(value = "/credits/ack/{threadId}", produces = "application/json")
    public ResponseEntity<CreditRest> creditAck(@PathVariable String threadId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Credit credit = creditRepository.findById(threadId).get();

        CreditBPM creditBPM = Utils.creditBPMFromCredit(credit);

        CreditAckBPM creditAckBPM = CreditAckBPM.newBuilder()
                .setCredit(creditBPM)
                .build();

        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadId(threadId)
                .setCreditAck(creditAckBPM)
                .build();

        try {
            if ( ! credit.getStatus().contentEquals(States.CREDIT_AGREED.name())) {
                // avoiding race condition
                credit.setStatus(States.CREDIT_AGREED_PENDING.name());
                credit = creditRepository.save(credit);
            } else {
                log.error("Credit state is already CREDIT_AGREED");
            }

            new OutboundHCSMessage(appData.getHCSCore())
                  .overrideEncryptedMessages(false)
                  .overrideMessageSignature(false)
                  .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            CreditRest creditRest = new CreditRest(credit, appData);
            
            return new ResponseEntity<>(creditRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

//    @Transactional
    @PostMapping(value = "/credits", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CreditRest> creditNew(@RequestBody CreditProposal creditCreate) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Instant now = Instant.now();
        Long seconds = now.getEpochSecond();
        int nanos = now.getNano();
        String threadId = Utils.getThreadId();

        Money value = Money.newBuilder()
                .setCurrencyCode(creditCreate.getCurrency())
                .setUnits(creditCreate.getAmount())
                .build();
        CreditBPM creditBPM = CreditBPM.newBuilder()
                .setAdditionalNotes(creditCreate.getAdditionalNotes())
                .setPayerName(creditCreate.getPayerName())
                .setRecipientName(creditCreate.getRecipientName())
                .setServiceRef(creditCreate.getReference())
                .setValue(value)
                .setCreatedDate(Utils.TimestampToDate(seconds, nanos))
                .setCreatedTime(Utils.TimestampToTime(seconds, nanos))
                .build();
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadId(threadId)
                .setCredit(creditBPM)
                .build();

        try {
            TransactionId transactionId = new TransactionId(appData.getHCSCore().getOperatorAccountId());

            Credit credit = new Credit();
            // copy data from new credit
            credit.setAdditionalNotes(creditCreate.getAdditionalNotes());
            credit.setAmount(creditCreate.getAmount());
            credit.setCurrency(creditCreate.getCurrency());
            credit.setPayerName(creditCreate.getPayerName());
            credit.setRecipientName(creditCreate.getRecipientName());
            credit.setReference(creditCreate.getReference());

            credit.setCreatedDate(Utils.TimestampToDate(seconds, nanos));
            credit.setCreatedTime(Utils.TimestampToTime(seconds, nanos));
            credit.setApplicationMessageId(Utils.TransactionIdToString(transactionId));
            credit.setThreadId(threadId);
            
            credit = creditRepository.save(credit);
            
            credit = creditRepository.findById(threadId).get();
            if ((credit.getStatus() == null) || ( ! credit.getStatus().contentEquals(States.CREDIT_PROPOSED.name()))) {
                // avoiding race condition
                credit.setStatus(States.CREDIT_PROPOSED_PENDING.name());
                credit = creditRepository.save(credit);
            } else {
                log.error("Credit state is already CREDIT_PROPOSED");
            }
            
            credit = creditRepository.save(credit);
            new OutboundHCSMessage(appData.getHCSCore())
                  .overrideEncryptedMessages(false)
                  .overrideMessageSignature(false)
                  .withFirstTransactionId(transactionId)
                  .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            CreditRest creditRest = new CreditRest(credit, appData);
            return new ResponseEntity<>(creditRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
//    private Sort sortByIdAsc() {
//        return new Sort(Sort.Direction.DESC, "threadId");
//    }
}
