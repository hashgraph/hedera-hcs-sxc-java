package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.restclasses.CreditProposal;
import com.hedera.hcsapp.restclasses.CreditRest;
import com.hedera.hcslib.consensus.OutboundHCSMessage;

import lombok.extern.log4j.Log4j2;
import proto.CreditAckBPM;
import proto.CreditBPM;
import proto.Money;
import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    public CreditsController() throws FileNotFoundException, IOException {

        appData = new AppData();
    }

    @GetMapping(value = "/credits/{user}", produces = "application/json")
    public ResponseEntity<List<CreditRest>> credits(@PathVariable String user) throws FileNotFoundException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        AppData appData = new AppData();
        List<Credit> creditList = new ArrayList<Credit>();
        List<CreditRest> restResponse = new ArrayList<CreditRest>();

        if (user == null) {
            creditList = (List<Credit>) creditRepository.findAll();
        } else {
            creditList = creditRepository.findAllCreditsForUsers(appData.getUserName(), user);
        }
        
        for (Credit credit : creditList) {
            restResponse.add(new CreditRest(credit, appData));
        }

        return new ResponseEntity<>(restResponse, headers, HttpStatus.OK);
    }

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
            TransactionId transactionId = new OutboundHCSMessage(appData.getHCSLib())
                  .overrideEncryptedMessages(false)
                  .overrideMessageSignature(false)
                  .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            CreditRest creditRest = new CreditRest(credit, appData);
            
            return new ResponseEntity<>(creditRest, headers, HttpStatus.OK);
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            log.error(e);
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
            TransactionId transactionId = new TransactionId(appData.getHCSLib().getOperatorAccountId());

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
            credit.setStatus(Enums.state.CREDIT_PROPOSED_PENDING.name());

            credit = creditRepository.save(credit);

            new OutboundHCSMessage(appData.getHCSLib())
                  .overrideEncryptedMessages(false)
                  .overrideMessageSignature(false)
                  .withFirstTransactionId(transactionId)
                  .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());

            log.info("Message sent successfully.");

            CreditRest creditRest = new CreditRest(credit, appData);
            return new ResponseEntity<>(creditRest, headers, HttpStatus.OK);
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            log.error(e);
            return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
