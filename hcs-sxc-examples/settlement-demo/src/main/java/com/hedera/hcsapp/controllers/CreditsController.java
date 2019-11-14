package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.appconfig.AppConfig;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.consensus.OutboundHCSMessage;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import proto.CreditBPM;
import proto.Money;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@Transactional
@RestController
public class CreditsController {
    
    @Autowired
    CreditRepository creditRepository;

    private static AppConfig appConfig;
    private Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();
    private static HCSLib hcsLib;
    private static int topicIndex = 0; // refers to the first topic ID in the config.yaml
    
    public CreditsController() throws FileNotFoundException, IOException {
        long appId = 0;
        
        appId = Long.parseLong(dotEnv.get("APP_ID"));
        hcsLib = new HCSLib(appId);
        appConfig = new AppConfig();
    }

    @GetMapping(value = "/credits", produces = "application/json")
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
    @PostMapping("/credits")
    Credit newCredit(@RequestBody Credit newCredit) {
        newCredit.setStatus(Enums.state.CREDIT_PENDING.name());
        
        Money value = Money.newBuilder()
                .setCurrencyCode(newCredit.getCurrency())
                .setUnits(newCredit.getAmount())
                .build();
        CreditBPM creditBPM = CreditBPM.newBuilder()
                .setMemo(newCredit.getMemo())
                .setPayerPublicKey(newCredit.getPayerPublicKey())
                .setRecipientPublicKey(newCredit.getRecipientPublicKey())
                .setServiceRef(newCredit.getServiceRef())
                .setValue(value)
                .build();
        
        try {
            new OutboundHCSMessage(hcsLib)
                  .overrideEncryptedMessages(false)
                  .overrideMessageSignature(false)
                  .sendMessage(topicIndex, creditBPM.toString());

            log.info("Message sent successfully.");
            newCredit = creditRepository.save(newCredit);
            log.info(creditRepository.count());
            return newCredit;
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            log.error(e);
            throw new ProcessingException(e);
        }
    }
}
