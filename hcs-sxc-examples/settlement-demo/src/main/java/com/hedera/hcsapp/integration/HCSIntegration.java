package com.hedera.hcsapp.integration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.hedera.hcsapp.Enums;
import com.hedera.hcsapp.appconfig.AppConfig;
import com.hedera.hcsapp.entities.Credit;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import com.hedera.hcslib.proto.java.ApplicationMessage;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import proto.CreditAckBPM;
import proto.CreditBPM;
import proto.SettlementBPM;

@Log4j2
@Component
public class HCSIntegration {

    private static AppConfig appConfig;
    private Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();
    private static HCSLib hcsLib;

    @Autowired
    CreditRepository creditRepository;
    
    public HCSIntegration() throws Exception {
        
        long appId = 0;

        if (dotEnv.get("APP_ID").isEmpty()) {
            log.error("APPID environment variable is not set - exiting");
            System.exit(0);
        }
        appId = Long.parseLong(dotEnv.get("APP_ID"));
        hcsLib = new HCSLib(appId);
        appConfig = new AppConfig();

        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsLib);
        onHCSMessageCallback.addObserver(message -> {
            processHCSMessage(message);
        });
    }
    
    public void processHCSMessage(byte[] message) {
        try {
            ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(message);
            
            SettlementBPM settlementBPM = SettlementBPM.parseFrom(applicationMessage.getBusinessProcessMessage().toByteArray());
            if (settlementBPM.hasCredit()) {
                CreditBPM creditBPM = settlementBPM.getCredit();
                String threadId = creditBPM.getThreadId();
                // update the credit state
                creditRepository.findById(threadId).ifPresentOrElse(
                        (credit) -> {
                            credit.setStatus(Enums.state.CREDIT_AWAIT_ACK.name());
                            creditRepository.save(credit);
                        },
                        () -> {
                            log.error("No credit found for threadId: " + threadId);
                        }
                );
            } else if (settlementBPM.hasCreditAck()) {
                CreditAckBPM creditAckBPM = settlementBPM.getCreditAck();
                String threadId = creditAckBPM.getThreadId();
                // update the credit state
                creditRepository.findById(threadId).ifPresentOrElse(
                        (credit) -> {
                            credit.setStatus(Enums.state.CREDIT_ACK.name());
                            creditRepository.save(credit);
                        },
                        () -> {
                            log.error("No credit found for threadId: " + threadId);
                        }
                );
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
