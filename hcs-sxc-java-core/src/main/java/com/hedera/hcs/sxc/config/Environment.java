package com.hedera.hcs.sxc.config;

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
import com.hedera.hcs.sxc.utils.StringUtils;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
@Log4j2
public final class Environment {

    /**
     * Manages configuration data which is either held in environment variables or a .env file
     */

    private Dotenv dotEnv;

    public Environment() {
        this("./config/.env");
    }
    public Environment(String fileName) {
        try {
            this.dotEnv = Dotenv.configure().filename(fileName).load();
            log.debug("Found .env file in " + fileName);
        } catch (Exception e) {
            log.warn("No " + fileName + " found.");
        }
    }

    private String getEnvValue(String environmentVariable) {
        String value = System.getProperty(environmentVariable);
        if (value == null){
            value = dotEnv.get(environmentVariable);
        }
        return value;
    }

    /**
     * Returns an Ed25519PrivateKey from the OPERATOR_KEY environment variable
     * @return Ed25519PrivateKey
     */
    public Ed25519PrivateKey getOperatorKey() {
        String operatorKey = getEnvValue("OPERATOR_KEY");
        return Ed25519PrivateKey.fromString(operatorKey);
    }

    /**
     * Returns a string representing the value of the OPERATOR_ID environment variable
     * @return String
     */
    public String getOperatorAccount() {
        return getEnvValue("OPERATOR_ID");
    }

    /**
     * Returns a string representing the value of the OPERATOR_ID environment variable
     * @return AccountId
     */
    public AccountId getOperatorAccountId() {
        String operatorId = getEnvValue("OPERATOR_ID");
        return AccountId.fromString(operatorId);
    }

    /**
     * Returns the app id
     */
    public int getAppId() {
        return Integer.parseInt(getEnvValue("APP_ID"));
    }

    
    public byte[] getMessageEncryptionKey() {
        return StringUtils.hexStringToByteArray(getEnvValue("ENCRYPTION_KEY"));
    }
    
    public Dotenv getDotEnv() {
        return this.dotEnv;
    }

}
