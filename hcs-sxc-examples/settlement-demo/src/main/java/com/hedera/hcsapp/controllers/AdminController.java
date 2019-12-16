package com.hedera.hcsapp.controllers;


import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.ByteString;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.States;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.entities.Settlement;
import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.consensus.OutboundHCSMessage;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.proto.java.AccountID;
import com.hedera.hcslib.proto.java.ApplicationMessage;
import com.hedera.hcslib.proto.java.ApplicationMessageChunk;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

import lombok.extern.log4j.Log4j2;

import com.hedera.hcslib.proto.java.ApplicationMessageId;
import com.hedera.hcslib.proto.java.Timestamp;

import proto.AdminDeleteBPM;
import proto.CreditBPM;
import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import proto.AdminStashDatabaseBPM;
import proto.AdminStashPopDatabaseBPM;
@Log4j2
@RestController
public class AdminController {

    @Autowired
    CreditRepository creditRepository;
    
    @Autowired
    SettlementRepository settlementRepository;
    
    @Autowired
    SettlementItemRepository settlementItemRepository;
    
    @Autowired
    AddressBookRepository addressBookRepository;

    private static AppData appData;

    public AdminController() throws FileNotFoundException, IOException {

        appData = new AppData();
    }

    @Transactional
    @GetMapping(value = "/admin/deletedata", produces = "application/json")
    public ResponseEntity<List<Credit>> deleteData() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        AdminDeleteBPM adminDeleteBPM = AdminDeleteBPM.newBuilder().build();
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadId("admin")
                .setAdminDelete(adminDeleteBPM)
                .build();
        try {
            new OutboundHCSMessage(appData.getHCSLib())
                .overrideEncryptedMessages(false)
                .overrideMessageSignature(false)
                .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
        
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }


    @Transactional
    @GetMapping(value = "/admin/stash-database", produces = "application/json")
    public ResponseEntity<List<Credit>> stashDatabase() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        AdminStashDatabaseBPM adminStashDatabaseBPM = AdminStashDatabaseBPM.newBuilder().build();
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadId("admin")
                .setAdminStashDatabaseBPM(adminStashDatabaseBPM)
                .build();
        try {
            new OutboundHCSMessage(appData.getHCSLib())
                .overrideEncryptedMessages(false)
                .overrideMessageSignature(false)
                .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
    
     @Transactional
    @GetMapping(value = "/admin/stash-pop-database", produces = "application/json")
    public ResponseEntity<List<Credit>> stashPopDatabase() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

         AdminStashPopDatabaseBPM adminStashPopDatabaseBPM = AdminStashPopDatabaseBPM.newBuilder().build();
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadId("admin")
                .setAdminStashPopDatabaseBPM(adminStashPopDatabaseBPM)
                .build();
        try {
            new OutboundHCSMessage(appData.getHCSLib())
                .overrideEncryptedMessages(false)
                .overrideMessageSignature(false)
                .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
}
