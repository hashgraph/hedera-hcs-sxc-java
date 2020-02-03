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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.integration.HCSMessages;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.restclasses.SettlementChannelProposal;
import com.hedera.hcsapp.restclasses.SettlementPaidOrComplete;
import com.hedera.hcsapp.restclasses.SettlementPaymentInit;
import com.hedera.hcsapp.restclasses.SettlementProposal;
import com.hedera.hcsapp.restclasses.SettlementRest;

import lombok.extern.log4j.Log4j2;
import proto.Money;
import proto.PaymentInitAckBPM;
import proto.PaymentInitBPM;
import proto.SettleCompleteBPM;
import proto.SettleInitAckBPM;
import proto.SettleInitBPM;
import proto.SettlePaidAckBPM;
import proto.SettlePaidBPM;
import proto.SettleProposeAckBPM;
import proto.SettleProposeBPM;
import proto.SettlementBPM;

@Log4j2
@RestController
public class SettlementsController {

    @Autowired
    SettlementItemRepository settlementItemRepository;

    @Autowired
    SettlementRepository settlementRepository;

    @Autowired
    CreditRepository creditRepository;

    private static AppData appData;
    private static int topicIndex = 0; // refers to the first topic ID in the config.yaml

    HttpHeaders headers = new HttpHeaders();

    public SettlementsController() throws Exception {
        appData = new AppData();
        headers.add("Content-Type", "application/json");
    }

    @GetMapping(value = "/settlements/{user}", produces = "application/json")
    public ResponseEntity<List<SettlementRest>> settlementsForUser(@PathVariable String user)
            throws FileNotFoundException, IOException {
        log.debug("/settlements/" + user);

        List<SettlementRest> settlementsList = new ArrayList<SettlementRest>();
        List<Settlement> settlements = settlementRepository.findAllSettlementsForUsers(appData.getUserName(), user);
        for (Settlement settlementfromDB : settlements) {
            SettlementRest settlementResponse = new SettlementRest(settlementfromDB, appData, settlementItemRepository,
                    creditRepository);
            settlementsList.add(settlementResponse);
        }
        return new ResponseEntity<>(settlementsList, headers, HttpStatus.OK);

    }

    @PostMapping(value = "/settlements", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settlementNew(@RequestBody SettlementProposal settleProposal)
            throws Exception {
        try {
            SettlementRest settlementResponse = HCSMessages.settlementNew(appData, creditRepository, settlementRepository, settlementItemRepository, settleProposal, false);
            return new ResponseEntity<>(settlementResponse, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeAck(@PathVariable String threadId) throws Exception {

        try {
            SettlementRest settlementRest = HCSMessages.settlementAck(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/proposechannel", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeChannel(
            @RequestBody SettlementChannelProposal settlementChannelProposal) throws Exception {

        String additionalNotes = settlementChannelProposal.getAdditionalNotes();
        String paymentChannelName = settlementChannelProposal.getPaymentChannelName();

        String threadId = settlementChannelProposal.getThreadId();
        try {
            SettlementRest settlementRest = HCSMessages.settlementInit(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false, additionalNotes, paymentChannelName);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/proposechannel/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeChannelAck(@PathVariable String threadId) throws Exception {
        try {
            SettlementRest settlementRest = HCSMessages.settleProposeChannelAck(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/paymentInit", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settlePaymentInit(@RequestBody SettlementPaymentInit settlementPaymentInit)
            throws Exception {

        String threadId = settlementPaymentInit.getThreadId();
        String payerAccountDetails = settlementPaymentInit.getPayerAccountDetails();
        String recipientAccountDetails = settlementPaymentInit.getRecipientAccountDetails();
        String additionalNotes = settlementPaymentInit.getAdditionalNotes();
        
        try {
            SettlementRest settlementRest = HCSMessages.settlePaymentInit(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false, payerAccountDetails, recipientAccountDetails, additionalNotes);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/paymentInit/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settlePaymentInitAck(@PathVariable String threadId) throws Exception {

        try {
            SettlementRest settlementRest = HCSMessages.settlePaymentInitAck(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/paid", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> paid(@RequestBody SettlementPaidOrComplete settlementPaid) throws Exception {

        String threadId = settlementPaid.getThreadId();
        String additionalNotes = settlementPaid.getAdditionalNotes();
        try {
            SettlementRest settlementRest = HCSMessages.settlePaymentPaid(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false, additionalNotes);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/paid/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> paidAck(@PathVariable String threadId) throws Exception {

        try {
            SettlementRest settlementRest = HCSMessages.settlePaymentPaidAck(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/complete", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> complete(@RequestBody SettlementPaidOrComplete settlementPaid) throws Exception {
        
        String threadId = settlementPaid.getThreadId();
        String additionalNotes = settlementPaid.getAdditionalNotes();
        try {
            SettlementRest settlementRest = HCSMessages.settlePaymentComplete(appData, settlementRepository, settlementItemRepository, creditRepository, threadId, false, additionalNotes);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }
}
