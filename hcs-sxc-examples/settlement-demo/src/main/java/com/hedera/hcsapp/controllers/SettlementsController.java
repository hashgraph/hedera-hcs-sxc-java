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

import com.google.gson.JsonArray;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.restclasses.SettlementProposal;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.consensus.OutboundHCSMessage;

import lombok.extern.log4j.Log4j2;
import proto.CreditAckBPM;
import proto.CreditBPM;
import proto.Money;
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
    
    public SettlementsController() throws FileNotFoundException, IOException {
        appData = new AppData();
    }
    
    @GetMapping(value = "/settlements/{user}", produces = "application/json")
    public ResponseEntity<List<SettlementProposal>> settlementsForUser(@PathVariable String user) throws FileNotFoundException, IOException {
        log.debug("/settlements/" + user);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");    

        List<SettlementProposal> settlementsList = new ArrayList<SettlementProposal>();
        List<Credit> creditList = new ArrayList<Credit>();
        List<Settlement> settlements = settlementRepository.findAllSettlementsForUsers(appData.getUserName(), user);
        for (Settlement settlementfromDB : settlements) {
            SettlementProposal settlementProposal = new SettlementProposal();
            settlementProposal.setAdditionalNotes(settlementfromDB.getAdditionalNotes());
            settlementProposal.setNetValue(settlementfromDB.getNetValue());
            settlementProposal.setCurrency(settlementfromDB.getCurrency());
            settlementProposal.setPayerName(settlementfromDB.getPayerName());
            settlementProposal.setRecipientName(settlementfromDB.getRecipientName());
            settlementProposal.setThreadId(settlementfromDB.getThreadId());
            settlementProposal.setApplicationMessageId(settlementfromDB.getApplicationMessageId());
            settlementProposal.setStatus(settlementfromDB.getStatus());
            settlementProposal.setCreatedDate(settlementfromDB.getCreatedDate());
            settlementProposal.setCreatedTime(settlementfromDB.getCreatedTime());

            List<SettlementItem> settlementItemsFromDB = settlementItemRepository.findAllSettlementItems(settlementfromDB.getThreadId());
            List<String> threadIds = new ArrayList<String>();
            for (SettlementItem settlementItem : settlementItemsFromDB) {
                threadIds.add(settlementItem.getId().getSettledThreadId());
                creditRepository.findById(settlementItem.getId().getSettledThreadId()).ifPresent(
                        (credit) -> {
                            creditList.add(credit);        
                        }
                );
                    
            }
            settlementProposal.setThreadIds(threadIds);
            settlementProposal.setCredits(creditList);
            settlementsList.add(settlementProposal);
        }
        if (settlementsList.size() != 0) {
            return new ResponseEntity<>(settlementsList, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(headers, HttpStatus.OK);
        }
    }
    @PostMapping(value = "/settlements", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Settlement> settlementNew(@RequestBody SettlementProposal settleProposal) throws Exception {
        log.debug("POST to /settlements/");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");    

        Instant now = Instant.now();
        Long seconds = now.getEpochSecond();
        int nanos = now.getNano();
        String threadId = seconds + "-" + nanos;
        
        Money value = Money.newBuilder()
                .setCurrencyCode(settleProposal.getCurrency())
                .setUnits(settleProposal.getNetValue())
                .build();
        SettleProposeBPM.Builder settleProposeBPM = SettleProposeBPM.newBuilder()
                .setAdditionalNotes(settleProposal.getAdditionalNotes())
                .setPayerName(settleProposal.getPayerName())
                .setRecipientName(settleProposal.getRecipientName())
                .setNetValue(value);
        
        for (String proposedThreadId : settleProposal.getThreadIds()) {
            settleProposeBPM.addThreadIds(proposedThreadId);
        }
                
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadId(threadId)
                .setSettlePropose(settleProposeBPM.build())
                .build();
        
        try {
            TransactionId transactionId = new TransactionId(appData.getHCSLib().getOperatorAccountId());

            Settlement settlement = new Settlement();
            // copy data 
            settlement.setAdditionalNotes(settleProposal.getAdditionalNotes());
            settlement.setCurrency(settleProposal.getCurrency());
            settlement.setNetValue(settleProposal.getNetValue());
            settlement.setPayerName(settleProposal.getPayerName());
            settlement.setRecipientName(settleProposal.getRecipientName());
            settlement.setStatus(Enums.state.SETTLE_PROPOSE_PENDING.name());
            settlement.setThreadId(threadId);
            settlement.setApplicationMessageId(Utils.TransactionIdToString(transactionId));
            settlement.setCreatedDate(Utils.TimestampToDate(seconds, nanos));
            settlement.setCreatedTime(Utils.TimestampToTime(seconds, nanos));

            settlement = settlementRepository.save(settlement);

            // now settlement items
            for (String settledThreadId : settleProposal.getThreadIds()) {
                SettlementItem settlementItem = new SettlementItem();
                settlementItem.setId(new SettlementItemId(settledThreadId, threadId));
                settlementItem = settlementItemRepository.save(settlementItem);
            }

            new OutboundHCSMessage(appData.getHCSLib())
                  .overrideEncryptedMessages(false)
                  .overrideMessageSignature(false)
                  .withFirstTransactionId(transactionId)
                  .sendMessage(topicIndex, settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            return new ResponseEntity<>(settlement, headers, HttpStatus.OK);
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            log.error(e);
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping(value = "/settlements/ack/{threadId}", produces = "application/json")
    public ResponseEntity<SettlementProposal> settleProposeAck(@PathVariable String threadId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");    

        SettlementProposal settlementProposal = new SettlementProposal();
        List<String> threadIds = new ArrayList<String>();
        
        Optional<Settlement> settlement = settlementRepository.findById(threadId);
        
        if (settlement.isPresent()) {
            Money value = Money.newBuilder()
                    .setCurrencyCode(settlement.get().getCurrency())
                    .setUnits(settlement.get().getNetValue())
                    .build();
            
            SettleProposeBPM.Builder settleProposeBPM = SettleProposeBPM.newBuilder()
                    .setAdditionalNotes(settlement.get().getAdditionalNotes())
                    .setNetValue(value)
                    .setPayerName(settlement.get().getPayerName())
                    .setRecipientName(settlement.get().getRecipientName());
            
            List<SettlementItem> settlementItems = settlementItemRepository.findAllSettlementItems(threadId);
            for (SettlementItem settlementItem : settlementItems) {
                settleProposeBPM.addThreadIds(settlementItem.getId().getSettledThreadId());
                threadIds.add(settlementItem.getId().getSettledThreadId());
            }
            
            SettleProposeAckBPM settleProposeAck = SettleProposeAckBPM.newBuilder()
                    .setSettlePropose(settleProposeBPM.build())
                    .build();
            
            SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                    .setThreadId(threadId)
                    .setSettleProposeAck(settleProposeAck)
                    .build();

            settlementProposal.setAdditionalNotes(settlement.get().getAdditionalNotes());
            settlementProposal.setCurrency(settlement.get().getCurrency());
            settlementProposal.setNetValue(settlement.get().getNetValue());
            settlementProposal.setPayerName(settlement.get().getPayerName());
            settlementProposal.setRecipientName(settlement.get().getRecipientName());
            settlementProposal.setThreadId(threadId);
            settlementProposal.setThreadIds(threadIds);
            settlementProposal.setStatus(settlement.get().getStatus());
            settlementProposal.setApplicationMessageId(settlement.get().getApplicationMessageId());
            settlementProposal.setCreatedDate(settlement.get().getCreatedDate());
            settlementProposal.setCreatedTime(settlement.get().getCreatedTime());
            
            try {
                TransactionId transactionId = new OutboundHCSMessage(appData.getHCSLib())
                      .overrideEncryptedMessages(false)
                      .overrideMessageSignature(false)
                      .sendMessage(topicIndex, settlementBPM.toByteArray());

                log.info("Message sent successfully.");

                return new ResponseEntity<>(settlementProposal, headers, HttpStatus.OK);

            } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
                log.error(e);
                return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
