package com.hedera.hcsapp.integration;

import org.springframework.stereotype.Component;

import com.hedera.hcsapp.appconfig.AppConfig;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import proto.CreditBPM;
import proto.SettlementBPM;

@Log4j2
@Component
public class HCSIntegration {

    private static AppConfig appConfig;
    private Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();
    private static HCSLib hcsLib;

    public HCSIntegration() throws Exception {
        
        long appId = 0;
        appId = Long.parseLong(dotEnv.get("APP_ID"));
        hcsLib = new HCSLib(appId);
        appConfig = new AppConfig();

        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsLib);
        onHCSMessageCallback.addObserver(message -> {
            processHCSMessage(message);
        });
    }
    
    public void processHCSMessage(String message) {
        try {
            SettlementBPM settlementBPM = SettlementBPM.parseFrom(message.getBytes());
            if (settlementBPM.hasCredit()) {
                // handle credit
                CreditBPM creditBPM = settlementBPM.getCredit();
                
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
