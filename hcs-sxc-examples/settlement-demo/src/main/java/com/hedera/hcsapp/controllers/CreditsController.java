package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hcsapp.AppData;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.Utils;
import com.hedera.hcsapp.appconfig.AppConfig;
import com.hedera.hcsapp.entities.AddressBook;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.repository.AddressBookRepository;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.consensus.OutboundHCSMessage;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import proto.CreditBPM;
import proto.Money;
import proto.SettlementBPM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@Transactional
@RestController
public class CreditsController {
    
    @Autowired
    CreditRepository creditRepository;
    @Autowired
    AddressBookRepository addressBookRepository;

    private static AppConfig appConfig;
    private Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();
    private static HCSLib hcsLib;
    private static int topicIndex = 0; // refers to the first topic ID in the config.yaml
    
    public CreditsController() throws FileNotFoundException, IOException {
        long appId = 0;
        
        appId = Long.parseLong(dotEnv.get("APPID"));
        hcsLib = new HCSLib(appId);
        appConfig = new AppConfig();
    }

    @GetMapping(value = "/credits/{user}", produces = "application/json")
    public List<Credit> credits(@PathVariable String user) throws FileNotFoundException, IOException {
        if (creditRepository.count() == 0) {
            // TODO remove this automatic data generation
            Credit credit = new Credit();
            credit.setTransactionId("0.0.1234-1111-11");
            credit.setThreadId(1);
            credit.setPayerPublicKey("302a300506032b6570032100cc8aa257dfac5c0881e462d8e25218ec4be10887cc2fe98d22493077134a1069");
            credit.setRecipientPublicKey("302a300506032b657003210009724535fe4ea8f536a2c278d5aceb36f969281115093299a02a6172808470c9");
            credit.setAmount(1);
            credit.setCurrency("USD");
            credit.setMemo("memo 1");
            credit.setServiceRef("service ref 1");
            credit.setStatus(Enums.state.CREDIT_PENDING.name());
            
            creditRepository.save(credit);
            
            credit = new Credit();
            credit.setTransactionId("0.0.1234-2222-22");
            credit.setThreadId(2);
            credit.setPayerPublicKey("302a300506032b657003210009724535fe4ea8f536a2c278d5aceb36f969281115093299a02a6172808470c9");
            credit.setRecipientPublicKey("302a300506032b6570032100cc8aa257dfac5c0881e462d8e25218ec4be10887cc2fe98d22493077134a1069");
            credit.setAmount(2);
            credit.setCurrency("EUR");
            credit.setMemo("memo 2");
            credit.setServiceRef("service ref 2");
            credit.setStatus(Enums.state.CREDIT_PENDING.name());
            
            creditRepository.save(credit);
        }
        
        JsonArray credits = new JsonArray();
        AppData appData = new AppData();
        List<Credit> creditList = new ArrayList<Credit>();
        
        if (user == null) {
            creditList = (List<Credit>) creditRepository.findAll(); 
        } else {
            // find the public key for this user
            AddressBook addressBook = addressBookRepository.findUserByName(user);
            if (addressBook != null) {
                creditList = creditRepository.findAllCreditsForKeys(appData.getPublicKey(), addressBook.getPublicKey());
            }
        }
        
        return creditList;
    }
    @PostMapping("/credits")
    Credit newCredit(@RequestBody Credit newCredit) {
        
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
        SettlementBPM settlementBPM = SettlementBPM.newBuilder()
                .setCredit(creditBPM)
                .build();
        
        try {
            TransactionId transactionId = new OutboundHCSMessage(hcsLib)
                  .overrideEncryptedMessages(false)
                  .overrideMessageSignature(false)
                  .sendMessage(topicIndex, settlementBPM.toString());

            log.info("Message sent successfully.");

            newCredit.setStatus(Enums.state.CREDIT_PENDING.name());
            long seconds = transactionId.getValidStart().getEpochSecond();
            int nanos = transactionId.getValidStart().getNano();
            String txId = transactionId.getAccountId().toString() + "-" + seconds + "-" + nanos;

            newCredit.setTransactionId(txId);
            newCredit.setCreatedDate(Utils.TimestampToDate(seconds, nanos));
            newCredit.setCreatedTime(Utils.TimestampToTime(seconds, nanos));
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
