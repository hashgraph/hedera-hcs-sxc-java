package com.hedera.tokendemo.config;

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

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.tokendemo.utils.Utils;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;

@Log4j2
@Component
public class AppData {

    private HCSCore hcsCore;
    private int topicIndex = 0; // refers to the first topic ID in the config.yaml
    private AccountId operatorId = new AccountId(0,0,0);
    private Dotenv dotEnv;
    private String tokenName = "";
    private String userName;
    private Ed25519PublicKey publicKey;
    private Ed25519PrivateKey operatorKey;

    public AppData(AppConfig appConfig) {
        log.info("AppData loading");
        this.userName = appConfig.demoUser;
        System.out.println("appConfig.demoUser");
        System.out.println(appConfig.demoUser);
        System.out.println("appConfig.environmentFilePath");
        System.out.println(appConfig.environmentFilePath);
        System.out.println("appConfig.configFile");
        System.out.println(appConfig.configFile);

        String environmentFile = appConfig.environmentFilePath + userName.toLowerCase() + ".env";
        File checkFileExists = new File(environmentFile);
        if (! checkFileExists.exists()) {
            log.debug(userName.toLowerCase() + ".env file not found, falling back to default.env");
            environmentFile = appConfig.environmentFilePath + "default.env";
        }

        this.dotEnv = Dotenv.configure().filename(environmentFile).load();
        this.operatorKey = Ed25519PrivateKey.fromString(dotEnv.get("OPERATOR_KEY"));
        this.publicKey = this.operatorKey.publicKey;
        this.operatorId = AccountId.fromString(dotEnv.get("OPERATOR_ID"));

        this.topicIndex = 0;
        try {
            log.debug("Creating HCS Core");
            this.hcsCore = new HCSCore()
                    .builder(this.userName, appConfig.configFile, environmentFile);
            if (this.hcsCore == null) {
                log.debug("HCS Core is null");
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    private String getOptionalEnvValue(String varName) throws Exception {
        String value = "";
        log.debug("Looking for " + varName + " in environment variables");
        if (System.getProperty(varName) != null) {
            value = System.getProperty(varName);
            log.debug(varName + " found in command line parameters");
        } else if ((this.dotEnv == null) || (this.dotEnv.get(varName) == null)) {
            value = "";
        } else {
            value = this.dotEnv.get(varName);
            log.debug(varName + " found in environment variables");
        }
        return value;
    }

    public HCSCore getHCSCore() {
        return this.hcsCore;
    }

    public int getTopicIndex() {
        return this.topicIndex;
    }
    public void setTopicIndex(int topicIndex) {
        this.topicIndex = topicIndex;
    }
    public Ed25519PublicKey getPublicKey() {
        return this.publicKey;
    }
    public Ed25519PrivateKey getOperatorKey() {
        return this.operatorKey;
    }
    public AccountId getOperatorId() {
        return this.operatorId;
    }
    public String getTokenName() {
        return this.tokenName;
    }
    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }
    public String getUserName() {
        return this.userName;
    }
    public String getPrompt() {
        if (this.userName.isEmpty()) {
            return "HCS-Token-demo:>";
        } else if (this.tokenName.isEmpty()) {
            return "HCS-Token-demo (user:" + Utils.capitalize(this.userName) +"):>";
        } else {
            return "HCS-Token-demo (user:" + Utils.capitalize(this.userName) +", token:" + this.tokenName + "):>";
        }
    }
}