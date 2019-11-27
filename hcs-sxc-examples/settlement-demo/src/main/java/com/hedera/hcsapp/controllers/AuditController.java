package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcsapp.restclasses.AuditThreadIds;
import com.hedera.hcsapp.restclasses.CreditProposal;
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
import java.util.Collections;
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
public class AuditController {

    @Autowired
    CreditRepository creditRepository;

    @Autowired
    SettlementRepository settlementRepository;

    private static AppData appData;
    private static int topicIndex = 0; // refers to the first topic ID in the config.yaml

    public AuditController() throws FileNotFoundException, IOException {

        appData = new AppData();
    }

    @GetMapping(value = "/audit", produces = "application/json")
    public ResponseEntity<AuditThreadIds> threadIds() throws FileNotFoundException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        List<String> threads = new ArrayList<String>();
        
        for (Credit credit : creditRepository.findAll()) {
            threads.add(credit.getThreadId() + " (Credit)");
        }
        
        for (Settlement settlement : settlementRepository.findAll()) {
            threads.add(settlement.getThreadId() + " (Settlement)");
        }
        
        Collections.sort(threads); 
        
        AuditThreadIds auditThreadIds = new AuditThreadIds();
        auditThreadIds.setThreadIds(threads);
        
        return new ResponseEntity<>(auditThreadIds, headers, HttpStatus.OK);
    }

}
