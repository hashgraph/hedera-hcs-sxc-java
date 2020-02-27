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
import org.springframework.web.bind.annotation.RestController;

import com.hedera.hcsapp.Statics;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Settlement;
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

@Log4j2
@RestController
public class SettlementsController {

    @Autowired
    private HCSMessages hcsMessages;

    @Autowired
    SettlementItemRepository settlementItemRepository;

    @Autowired
    SettlementRepository settlementRepository;

    @Autowired
    CreditRepository creditRepository;

    HttpHeaders headers = new HttpHeaders();

    public SettlementsController() throws Exception {
        headers.add("Content-Type", "application/json");
    }

    @GetMapping(value = "/settlements/{user}", produces = "application/json")
    public ResponseEntity<List<SettlementRest>> settlementsForUser(@PathVariable String user)
            throws Exception {
        log.debug("/settlements/" + user);

        List<SettlementRest> settlementsList = new ArrayList<SettlementRest>();
        List<Settlement> settlements = settlementRepository.findAllSettlementsForUsers(Statics.getAppData().getUserName(), user);
        for (Settlement settlementfromDB : settlements) {
            SettlementRest settlementResponse = new SettlementRest(settlementfromDB, Statics.getAppData(), settlementItemRepository,
                    creditRepository);
            settlementsList.add(settlementResponse);
        }
        return new ResponseEntity<>(settlementsList, headers, HttpStatus.OK);

    }

    @PostMapping(value = "/settlements", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settlementNew(@RequestBody SettlementProposal settleProposal)
            throws Exception {
        try {
            SettlementRest settlementResponse = hcsMessages.settlementNew(Statics.getAppData(), settleProposal);
            return new ResponseEntity<>(settlementResponse, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeAck(@PathVariable String threadId) throws Exception {

        try {
            SettlementRest settlementRest = hcsMessages.settlementAck(Statics.getAppData(), threadId, false);
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
            SettlementRest settlementRest = hcsMessages.settlementInit(Statics.getAppData(), threadId, false, additionalNotes, paymentChannelName);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/proposechannel/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeChannelAck(@PathVariable String threadId) throws Exception {
        try {
            SettlementRest settlementRest = hcsMessages.settleProposeChannelAck(Statics.getAppData(), threadId, false);
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
            SettlementRest settlementRest = hcsMessages.settlePaymentInit(Statics.getAppData(), threadId, false, payerAccountDetails, recipientAccountDetails, additionalNotes);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/paymentInit/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settlePaymentInitAck(@PathVariable String threadId) throws Exception {

        try {
            SettlementRest settlementRest = hcsMessages.settlePaymentInitAck(Statics.getAppData(), threadId, false);
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
            SettlementRest settlementRest = hcsMessages.settlePaymentPaid(Statics.getAppData(), threadId, false, additionalNotes);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }

    @PostMapping(value = "/settlements/paid/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> paidAck(@PathVariable String threadId) throws Exception {

        try {
            SettlementRest settlementRest = hcsMessages.settlePaymentPaidAck(Statics.getAppData(), threadId, false);
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
            SettlementRest settlementRest = hcsMessages.settlePaymentComplete(Statics.getAppData(), threadId, false, additionalNotes);
            return new ResponseEntity<>(settlementRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            return Utils.serverError();
        }
    }
}
