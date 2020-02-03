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

//    @Transactional
    @PostMapping(value = "/settlements", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settlementNew(@RequestBody SettlementProposal settleProposal)
            throws Exception {
        log.debug("POST to /settlements/");

        Instant now = Instant.now();
        Long seconds = now.getEpochSecond();
        int nanos = now.getNano();

        String threadId = Utils.getThreadId();

        Money value = Money.newBuilder().setCurrencyCode(settleProposal.getCurrency())
                .setUnits(settleProposal.getNetValue()).build();
        SettleProposeBPM.Builder settleProposeBPM = SettleProposeBPM.newBuilder()
                .setAdditionalNotes(settleProposal.getAdditionalNotes()).setPayerName(settleProposal.getPayerName())
                .setRecipientName(settleProposal.getRecipientName())
                .setCreatedDate(Utils.TimestampToDate(seconds, nanos))
                .setCreatedTime(Utils.TimestampToTime(seconds, nanos)).setNetValue(value);

        for (String proposedThreadId : settleProposal.getThreadIds()) {
            settleProposeBPM.addThreadIDs(proposedThreadId);
        }

        SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                .setSettlePropose(settleProposeBPM.build()).build();

        try {
            TransactionId transactionId = new TransactionId(appData.getHCSCore().getOperatorAccountId());

            Settlement settlement = new Settlement();
            // copy data
            settlement.setAdditionalNotes(settleProposal.getAdditionalNotes());
            settlement.setCurrency(settleProposal.getCurrency());
            settlement.setNetValue(settleProposal.getNetValue());
            settlement.setPayerName(settleProposal.getPayerName());
            settlement.setRecipientName(settleProposal.getRecipientName());
            settlement.setThreadId(threadId);
            settlement.setApplicationMessageId(Utils.TransactionIdToString(transactionId));
            settlement.setCreatedDate(Utils.TimestampToDate(seconds, nanos));
            settlement.setCreatedTime(Utils.TimestampToTime(seconds, nanos));

            settlement = settlementRepository.save(settlement);

            settlement = settlementRepository.findById(threadId).get();
            if ((settlement.getStatus() == null)
                    || (!settlement.getStatus().contentEquals(States.SETTLE_PROPOSED.name()))) {
                settlement.setStatus(States.SETTLE_PROPOSED_PENDING.name());
                settlement = settlementRepository.save(settlement);
            } else {
                log.error("Settlement state is already SETTLEMENT_PROPOSED");
            }

            // now settlement items
            for (String settledThreadId : settleProposal.getThreadIds()) {
                SettlementItem settlementItem = new SettlementItem();
                settlementItem.setId(new SettlementItemId(settledThreadId, threadId));
                settlementItem = settlementItemRepository.save(settlementItem);
            }

            new OutboundHCSMessage(appData.getHCSCore()).overrideEncryptedMessages(false).overrideMessageSignature(false)
                    .withFirstTransactionId(transactionId).sendMessage(topicIndex, settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            SettlementRest settlementResponse = new SettlementRest(settlement, appData, settlementItemRepository,
                    creditRepository);
            return new ResponseEntity<>(settlementResponse, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

//    @Transactional
    @PostMapping(value = "/settlements/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PROPOSED.name())) {
                SettleProposeBPM.Builder settleProposeBPM = SettleProposeBPM.newBuilder()
                        .setAdditionalNotes(settlement.get().getAdditionalNotes()).setNetValue(Utils.moneyFromSettlement(settlement.get()))
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName());
    
                List<SettlementItem> settlementItems = settlementItemRepository.findAllSettlementItems(threadId);
                for (SettlementItem settlementItem : settlementItems) {
                    settleProposeBPM.addThreadIDs(settlementItem.getId().getSettledThreadId());
                }
    
                SettleProposeAckBPM settleProposeAck = SettleProposeAckBPM.newBuilder()
                        .setSettlePropose(settleProposeBPM.build()).build();
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettleProposeAck(settleProposeAck).build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_AGREED);
            } else {
                log.error("Status is not " + States.SETTLE_PROPOSED.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }

    }

//    @Transactional
    @PostMapping(value = "/settlements/proposechannel", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeChannel(
            @RequestBody SettlementChannelProposal settlementChannelProposal) throws Exception {

        String threadId = settlementChannelProposal.getThreadId();
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            if (settlement.get().getStatus().contentEquals(States.SETTLE_AGREED.name())) {
                settlement.get().setAdditionalNotes(settlementChannelProposal.getAdditionalNotes());
                settlement.get().setPaymentChannelName(settlementChannelProposal.getPaymentChannelName());
    
                SettleInitBPM.Builder settleInitBPM = SettleInitBPM.newBuilder()
                        .setAdditionalNotes(settlementChannelProposal.getAdditionalNotes())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()))
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPaymentChannelName(settlementChannelProposal.getPaymentChannelName());
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId).setSettleInit(settleInitBPM)
                        .build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_PAY_CHANNEL_PROPOSED);
            } else {
                log.error("Status is not " + States.SETTLE_AGREED.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }

    }

//    @Transactional
    @PostMapping(value = "/settlements/proposechannel/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeChannelAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_CHANNEL_PROPOSED.name())) {
                SettleInitBPM.Builder settleInitBPM = SettleInitBPM.newBuilder()
                        .setAdditionalNotes(settlement.get().getAdditionalNotes())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()))
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPaymentChannelName(settlement.get().getPaymentChannelName());
    
                SettleInitAckBPM.Builder settleInitAckBPM = SettleInitAckBPM.newBuilder().setSettleInit(settleInitBPM);
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettleInitAck(settleInitAckBPM).build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_PAY_CHANNEL_AGREED);
            } else {
                log.error("Status is not " + States.SETTLE_PAY_CHANNEL_PROPOSED.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }

    }

//    @Transactional
    @PostMapping(value = "/settlements/paymentInit", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settlePaymentInit(@RequestBody SettlementPaymentInit settlementPaymentInit)
            throws Exception {

        String threadId = settlementPaymentInit.getThreadId();

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_CHANNEL_AGREED.name())) {
    
                PaymentInitBPM.Builder paymentInitBPM = PaymentInitBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPayerAccountDetails(settlementPaymentInit.getPayerAccountDetails())
                        .setRecipientAccountDetails(settlementPaymentInit.getRecipientAccountDetails())
                        .setAdditionalNotes(settlementPaymentInit.getAdditionalNotes())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setPaymentInit(paymentInitBPM).build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_PAY_PROPOSED);
            } else {
                log.error("Status is not " + States.SETTLE_PAY_CHANNEL_AGREED.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }
    }

//    @Transactional
    @PostMapping(value = "/settlements/paymentInit/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settlePaymentInitAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_PROPOSED.name())) {
                PaymentInitBPM.Builder paymentInitBPM = PaymentInitBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setPayerAccountDetails(settlement.get().getPayerAccountDetails())
                        .setRecipientAccountDetails(settlement.get().getRecipientAccountDetails())
                        .setAdditionalNotes(settlement.get().getAdditionalNotes())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                PaymentInitAckBPM.Builder paymentInitAckBPM = PaymentInitAckBPM.newBuilder().setPaymentInit(paymentInitBPM);
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setPaymentInitAck(paymentInitAckBPM).build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_PAY_AGREED);
            } else {
                log.error("Status is not " + States.SETTLE_PAY_PROPOSED.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }

    }

//  @Transactional
    @PostMapping(value = "/settlements/paid", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> paid(@RequestBody SettlementPaidOrComplete settlementPaid) throws Exception {

        String threadId = settlementPaid.getThreadId();

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_PAY_ACK.name())) {
                SettlePaidBPM.Builder settlePaidBPM = SettlePaidBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setAdditionalNotes(settlementPaid.getAdditionalNotes())
                        .setPaymentReference(settlement.get().getPaymentReference())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettlePayment(settlePaidBPM).build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_RCPT_REQUESTED);
            } else {
                log.error("Status is not " + States.SETTLE_PAY_ACK.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }

    }

  //@Transactional
    @PostMapping(value = "/settlements/paid/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> paidAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_RCPT_REQUESTED.name())) {
                SettlePaidBPM.Builder settlePaidBPM = SettlePaidBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setAdditionalNotes(settlement.get().getAdditionalNotes())
                        .setPaymentReference(settlement.get().getPaymentReference())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlePaidAckBPM.Builder settlePaidAckBPM = SettlePaidAckBPM.newBuilder()
                        .setSettlePaid(settlePaidBPM);
                
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettlePaymentAck(settlePaidAckBPM).build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_RCPT_CONFIRMED);
            } else {
                log.error("Status is not " + States.SETTLE_RCPT_REQUESTED.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }
    }

//  @Transactional
    @PostMapping(value = "/settlements/complete", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> complete(@RequestBody SettlementPaidOrComplete settlementPaid) throws Exception {
        
        String threadId = settlementPaid.getThreadId();

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {
            if (settlement.get().getStatus().contentEquals(States.SETTLE_RCPT_CONFIRMED.name())) {
                SettleCompleteBPM.Builder settleCompleteBPM = SettleCompleteBPM.newBuilder()
                        .setPayerName(settlement.get().getPayerName())
                        .setRecipientName(settlement.get().getRecipientName())
                        .setAdditionalNotes(settlementPaid.getAdditionalNotes())
                        .setPaymentReference(settlement.get().getPaymentReference())
                        .setNetValue(Utils.moneyFromSettlement(settlement.get()));
    
                SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadID(threadId)
                        .setSettleComplete(settleCompleteBPM).build();
    
                return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_PAY_CONFIRMED);
            } else {
                log.error("Status is not " + States.SETTLE_RCPT_CONFIRMED.name() + ", not performing update");
                return Utils.serverError();
            }
        } else {
            return Utils.serverError();
        }

    }

    private ResponseEntity<SettlementRest> saveAndSendSettlement(SettlementBPM settlementBPM, Settlement settlement,
            States newState) throws Exception {
        
        try {

            if ( ! settlement.getStatus().contentEquals(newState.name())) {
                settlement.setStatus(newState.name() + "_PENDING");
            } else {
                log.error("Settlement state is already " + newState.name());
            }

            Settlement newSettlement = settlementRepository.save(settlement);

            new OutboundHCSMessage(appData.getHCSCore()).overrideEncryptedMessages(false)
                    .overrideMessageSignature(false).sendMessage(topicIndex, settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            SettlementRest settlementResponse = new SettlementRest(newSettlement, appData, settlementItemRepository, creditRepository);
            return new ResponseEntity<>(settlementResponse, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
}
