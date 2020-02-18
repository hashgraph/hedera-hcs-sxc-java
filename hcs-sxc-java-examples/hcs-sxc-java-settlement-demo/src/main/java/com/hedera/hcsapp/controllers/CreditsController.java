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

import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Statics;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.integration.HCSMessages;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcsapp.restclasses.CreditProposal;
import com.hedera.hcsapp.restclasses.CreditRest;

import lombok.extern.log4j.Log4j2;
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
    HCSMessages hcsMessages;
    
    @Autowired
    CreditRepository creditRepository;

    @Autowired
    AddressBookRepository addressBookRepository;

    public CreditsController() throws Exception {
    }

    @GetMapping(value = "/credits/{user}", produces = "application/json")
    public ResponseEntity<List<CreditRest>> credits(@PathVariable String user) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        List<Credit> creditList = new ArrayList<Credit>();
        List<CreditRest> restResponse = new ArrayList<CreditRest>();

        if (user == null) {
            creditList = (List<Credit>) creditRepository.findAllDesc();
        } else {
            creditList = creditRepository.findAllCreditsForUsers(Statics.getAppData().getUserName(), user);
        }
        
        for (Credit credit : creditList) {
            restResponse.add(new CreditRest(credit, Statics.getAppData()));
        }

        return new ResponseEntity<>(restResponse, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/credits/ack/{threadId}", produces = "application/json")
    public ResponseEntity<CreditRest> creditAck(@PathVariable String threadId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        try {
            CreditRest creditRest = hcsMessages.creditAck(Statics.getAppData(), threadId, false);
            return new ResponseEntity<>(creditRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    @PostMapping(value = "/credits", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CreditRest> creditNew(@RequestBody CreditProposal creditCreate) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        try {
            CreditRest creditRest = hcsMessages.creditNew(Statics.getAppData(), creditCreate);
            return new ResponseEntity<>(creditRest, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
}