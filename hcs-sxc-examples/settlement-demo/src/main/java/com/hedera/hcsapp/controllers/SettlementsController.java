package com.hedera.hcsapp.controllers;

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
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.restclasses.PaymentSent;
import com.hedera.hcsapp.restclasses.SettlementChannelProposal;
import com.hedera.hcsapp.restclasses.SettlementPaidOrComplete;
import com.hedera.hcsapp.restclasses.SettlementPaymentInit;
import com.hedera.hcsapp.restclasses.SettlementProposal;
import com.hedera.hcsapp.restclasses.SettlementRest;
import com.hedera.hcslib.consensus.OutboundHCSMessage;

import lombok.extern.log4j.Log4j2;
import proto.Money;
import proto.PaymentInitAckBPM;
import proto.PaymentInitBPM;
import proto.PaymentSentAckBPM;
import proto.PaymentSentBPM;
import proto.SettleCompleteAckBPM;
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

    public SettlementsController() throws FileNotFoundException, IOException {
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
            settleProposeBPM.addThreadIds(proposedThreadId);
        }

        SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                .setSettlePropose(settleProposeBPM.build()).build();

        try {
            TransactionId transactionId = new TransactionId(appData.getHCSLib().getOperatorAccountId());

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
                    || (!settlement.getStatus().contentEquals(States.SETTLEMENT_PROPOSED.name()))) {
                settlement.setStatus(States.SETTLEMENT_PROPOSED_PENDING.name());
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

            new OutboundHCSMessage(appData.getHCSLib()).overrideEncryptedMessages(false).overrideMessageSignature(false)
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

            SettleProposeBPM.Builder settleProposeBPM = SettleProposeBPM.newBuilder()
                    .setAdditionalNotes(settlement.get().getAdditionalNotes()).setNetValue(moneyFromSettlement(settlement.get()))
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName());

            List<SettlementItem> settlementItems = settlementItemRepository.findAllSettlementItems(threadId);
            for (SettlementItem settlementItem : settlementItems) {
                settleProposeBPM.addThreadIds(settlementItem.getId().getSettledThreadId());
            }

            SettleProposeAckBPM settleProposeAck = SettleProposeAckBPM.newBuilder()
                    .setSettlePropose(settleProposeBPM.build()).build();

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setSettleProposeAck(settleProposeAck).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLEMENT_AGREED);
        } else {
            return serverError();
        }

    }

//    @Transactional
    @PostMapping(value = "/settlements/proposechannel", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeChannel(
            @RequestBody SettlementChannelProposal settlementChannelProposal) throws Exception {

        String threadId = settlementChannelProposal.getThreadId();
        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            settlement.get().setAdditionalNotes(settlementChannelProposal.getAdditionalNotes());
            settlement.get().setPaymentChannelName(settlementChannelProposal.getPaymentChannelName());

            SettleInitBPM.Builder settleInitBPM = SettleInitBPM.newBuilder()
                    .setAdditionalNotes(settlementChannelProposal.getAdditionalNotes())
                    .setNetValue(moneyFromSettlement(settlement.get()))
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setPaymentChannelName(settlementChannelProposal.getPaymentChannelName());

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId).setSettleInit(settleInitBPM)
                    .build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_INIT_AWAIT_ACK);
        } else {
            return serverError();
        }

    }

//    @Transactional
    @PostMapping(value = "/settlements/proposechannel/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settleProposeChannelAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            SettleInitBPM.Builder settleInitBPM = SettleInitBPM.newBuilder()
                    .setAdditionalNotes(settlement.get().getAdditionalNotes())
                    .setNetValue(moneyFromSettlement(settlement.get()))
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setPaymentChannelName(settlement.get().getPaymentChannelName());

            SettleInitAckBPM.Builder settleInitAckBPM = SettleInitAckBPM.newBuilder().setSettleInit(settleInitBPM);

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setSettleInitAck(settleInitAckBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_INIT_ACK);
        } else {
            return serverError();
        }

    }

//    @Transactional
    @PostMapping(value = "/settlements/paymentInit", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> settlePaymentInit(@RequestBody SettlementPaymentInit settlementPaymentInit)
            throws Exception {

        String threadId = settlementPaymentInit.getThreadId();

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            PaymentInitBPM.Builder paymentInitBPM = PaymentInitBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setPayerAccountDetails(settlementPaymentInit.getPayerAccountDetails())
                    .setRecipientAccountDetails(settlementPaymentInit.getRecipientAccountDetails())
                    .setAdditionalNotes(settlementPaymentInit.getAdditionalNotes())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setPaymentInit(paymentInitBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.PAYMENT_INIT_AWAIT_ACK);
        } else {
            return serverError();
        }
    }

//    @Transactional
    @PostMapping(value = "/settlements/paymentInit/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> settlePaymentInitAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            PaymentInitBPM.Builder paymentInitBPM = PaymentInitBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setPayerAccountDetails(settlement.get().getPayerAccountDetails())
                    .setRecipientAccountDetails(settlement.get().getRecipientAccountDetails())
                    .setAdditionalNotes(settlement.get().getAdditionalNotes())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            PaymentInitAckBPM.Builder paymentInitAckBPM = PaymentInitAckBPM.newBuilder().setPaymentInit(paymentInitBPM);

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setPaymentInitAck(paymentInitAckBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.PAYMENT_INIT_ACK);
        } else {
            return serverError();
        }

    }

//  @Transactional
    @PostMapping(value = "/settlements/paymentSent", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> paymentSent(@RequestBody PaymentSent paymentSent) throws Exception {

        String threadId = paymentSent.getThreadId();

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            PaymentSentBPM.Builder paymentSentBPM = PaymentSentBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setPayerAccountDetails(settlement.get().getPayerAccountDetails())
                    .setRecipientAccountDetails(settlement.get().getRecipientAccountDetails())
                    .setAdditionalNotes(paymentSent.getAdditionalNotes())
                    .setPaymentReference(paymentSent.getPaymentReference())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setPaymentSent(paymentSentBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.PAYMENT_SENT_AWAIT_ACK);
        } else {
            return serverError();
        }

    }

//@Transactional
    @PostMapping(value = "/settlements/paymentSent/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> paymentSentAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            PaymentSentBPM.Builder paymentSentBPM = PaymentSentBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setPayerAccountDetails(settlement.get().getPayerAccountDetails())
                    .setRecipientAccountDetails(settlement.get().getRecipientAccountDetails())
                    .setAdditionalNotes(settlement.get().getAdditionalNotes())
                    //.setPaymentReference(settlement.get().getPaymentReference())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            PaymentSentAckBPM.Builder paymentSentAckBPM = PaymentSentAckBPM.newBuilder().setPaymentSent(paymentSentBPM);

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setPaymentSentAck(paymentSentAckBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.PAYMENT_SENT_ACK);
        } else {
            return serverError();
        }
    }

//  @Transactional
    @PostMapping(value = "/settlements/paid", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> paid(@RequestBody SettlementPaidOrComplete settlementPaid) throws Exception {

        String threadId = settlementPaid.getThreadId();

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            SettlePaidBPM.Builder settlePaidBPM = SettlePaidBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setAdditionalNotes(settlementPaid.getAdditionalNotes())
                    .setPaymentReference(settlement.get().getPaymentReference())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setSettlePayment(settlePaidBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_PAID_AWAIT_ACK);
        } else {
            return serverError();
        }

    }

  //@Transactional
    @PostMapping(value = "/settlements/paid/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> paidAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            SettlePaidBPM.Builder settlePaidBPM = SettlePaidBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setAdditionalNotes(settlement.get().getAdditionalNotes())
                    .setPaymentReference(settlement.get().getPaymentReference())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            SettlePaidAckBPM.Builder settlePaidAckBPM = SettlePaidAckBPM.newBuilder()
                    .setSettlePaid(settlePaidBPM);
            
            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setSettlePaymentAck(settlePaidAckBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_PAID_ACK);
        } else {
            return serverError();
        }
    }

//  @Transactional
    @PostMapping(value = "/settlements/complete", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SettlementRest> complete(@RequestBody SettlementPaidOrComplete settlementPaid) throws Exception {
        
        String threadId = settlementPaid.getThreadId();

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            SettleCompleteBPM.Builder settleCompleteBPM = SettleCompleteBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setAdditionalNotes(settlementPaid.getAdditionalNotes())
                    .setPaymentReference(settlement.get().getPaymentReference())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setSettleComplete(settleCompleteBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_COMP_AWAIT_ACK);
        } else {
            return serverError();
        }

    }

  //@Transactional
    @PostMapping(value = "/settlements/complete/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementRest> completeAck(@PathVariable String threadId) throws Exception {

        Optional<Settlement> settlement = settlementRepository.findById(threadId);

        if (settlement.isPresent()) {

            SettleCompleteBPM.Builder settleCompleteBPM = SettleCompleteBPM.newBuilder()
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName())
                    .setAdditionalNotes(settlement.get().getAdditionalNotes())
                    .setPaymentReference(settlement.get().getPaymentReference())
                    .setNetValue(moneyFromSettlement(settlement.get()));

            SettleCompleteAckBPM.Builder settleCompleteAckBPM = SettleCompleteAckBPM.newBuilder()
                    .setSettlePaid(settleCompleteBPM);
            
            SettlementBPM settlementBPM = SettlementBPM.newBuilder().setThreadId(threadId)
                    .setSettleCompleteAck(settleCompleteAckBPM).build();

            return saveAndSendSettlement(settlementBPM, settlement.get(), States.SETTLE_COMPLETE_ACK);
        } else {
            return serverError();
        }
    }
    private ResponseEntity<SettlementRest> saveAndSendSettlement(SettlementBPM settlementBPM, Settlement settlement,
            States newState) throws Exception {
        
        String checkState = newState.name() + "_PENDING";

        try {

            if ( ! settlement.getStatus().contentEquals(newState.name())) {
                settlement.setStatus(newState.name() + "_PENDING");
            } else {
                log.error("Settlement state is already " + newState.name());
            }

            Settlement newSettlement = settlementRepository.save(settlement);

            TransactionId transactionId = new OutboundHCSMessage(appData.getHCSLib()).overrideEncryptedMessages(false)
                    .overrideMessageSignature(false).sendMessage(topicIndex, settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            SettlementRest settlementResponse = new SettlementRest(newSettlement, appData, settlementItemRepository, creditRepository);
            return new ResponseEntity<>(settlementResponse, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    private ResponseEntity<SettlementRest> serverError() {

        return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private Money moneyFromSettlement(Settlement settlement) {
        return Money.newBuilder().setCurrencyCode(settlement.getCurrency())
                .setUnits(settlement.getNetValue()).build();

    }
}
