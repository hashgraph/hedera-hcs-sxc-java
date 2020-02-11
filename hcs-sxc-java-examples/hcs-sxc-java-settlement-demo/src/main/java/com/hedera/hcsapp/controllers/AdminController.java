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

import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.repository.SettlementItemRepository;
import com.hedera.hcsapp.repository.SettlementRepository;

import lombok.extern.log4j.Log4j2;

import proto.AdminDeleteBPM;
import proto.SettlementBPM;

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

    public AdminController() throws Exception {

        appData = new AppData();
    }

    @Transactional
    @GetMapping(value = "/admin/deletedata", produces = "application/json")
    public ResponseEntity<List<Credit>> deleteData() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        AdminDeleteBPM adminDeleteBPM = AdminDeleteBPM.newBuilder().build();
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setThreadID("admin")
                .setAdminDelete(adminDeleteBPM)
                .build();
        try {
            new OutboundHCSMessage(appData.getHCSCore())
                //.overrideEncryptedMessages(false)
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
                .setThreadID("admin")
                .setAdminStashDatabaseBPM(adminStashDatabaseBPM)
                .build();
        try {
            new OutboundHCSMessage(appData.getHCSCore())
                //.overrideEncryptedMessages(false)
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
                .setThreadID("admin")
                .setAdminStashPopDatabaseBPM(adminStashPopDatabaseBPM)
                .build();
        try {
            new OutboundHCSMessage(appData.getHCSCore())
                //.overrideEncryptedMessages(false)
                .overrideMessageSignature(false)
                .sendMessage(appData.getTopicIndex(), settlementBPM.toByteArray());
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
}
