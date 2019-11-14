package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.repository.CreditRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Transactional
@RestController
public class SettlementController {
    
    @Autowired
    CreditRepository creditRepository;

    @RequestMapping(value = "/credits", method = RequestMethod.GET, produces = "application/json")
    public List<Credit> creditList() {
        if (creditRepository.count() == 0) {
            // TODO remove this automatic data generation
            Credit credit = new Credit();
            credit.setTransactionId("0.0.1234-1111-11");
            credit.setThreadId(1);
            credit.setPayerPublicKey("payer pub key 1");
            credit.setRecipientPublicKey("recipient pub key 1");
            credit.setAmount(1);
            credit.setCurrency("USD");
            credit.setMemo("memo 1");
            credit.setServiceRef("service ref 1");
            credit.setStatus(Enums.state.CREDIT_PENDING.name());
            
            creditRepository.save(credit);
            
            credit = new Credit();
            credit.setTransactionId("0.0.1234-2222-22");
            credit.setThreadId(2);
            credit.setPayerPublicKey("payer pub key 2");
            credit.setRecipientPublicKey("recipient pub key 2");
            credit.setAmount(2);
            credit.setCurrency("EUR");
            credit.setMemo("memo 2");
            credit.setServiceRef("service ref 2");
            credit.setStatus(Enums.state.CREDIT_PENDING.name());
            
            creditRepository.save(credit);
        }
        return (List<Credit>) creditRepository.findAll();
    }
}
