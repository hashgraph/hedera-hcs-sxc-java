package com.hedera.hcsapp;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.dockercomposereader.DockerCompose;
import com.hedera.hcsapp.dockercomposereader.DockerComposeReader;
import com.hedera.hcsapp.dockercomposereader.DockerService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class AppData {

    private HCSCore hcsCore;
    private int topicIndex = 0; // refers to the first topic ID in the config.yaml
    private String publicKey = "";
    private String userName = "";
    private long appId = 0;
    List<AppClient> appClients = new ArrayList<>();
    private int webPort = 8080;
    private Dotenv dotEnv;

    private String getEnvValue(String varName) throws Exception {
        String value = "";
        log.info("Looking for " + varName + " in environment variables");
        if (System.getProperty(varName) != null) {
            value = System.getProperty(varName);
            log.info(varName + " found in command line parameters");
        } else if ((this.dotEnv.get(varName) == null) || (this.dotEnv.get(varName).isEmpty())) {
            log.error(varName + " environment variable is not set");
            log.error(varName + " environment variable not found in ./config/.env");
            log.error(varName + " environment variable not found in command line parameters");
        } else {
            value = this.dotEnv.get(varName);
            log.info(varName + " found in environment variables");
        }
        return value;
    }

    private long getEnvValueLong(String varName) throws Exception {
        return Long.parseLong(getEnvValue(varName));
    }

    private int getEnvValueInt(String varName) throws Exception {
        return Integer.parseInt(getEnvValue(varName));
    }

    public AppData() throws Exception {

        this.hcsCore = new HCSCore(this.appId);
        this.dotEnv = hcsCore.getEnvironment();
        // just check if set
        getEnvValue("OPERATOR_KEY");
        this.appId = getEnvValueLong("APP_ID");
        if (this.appId != 0) {
            this.hcsCore = new HCSCore(this.appId);
        }
        DockerCompose dockerCompose = DockerComposeReader.parse();

        if (System.getProperty("server.port") != null) {
            this.webPort = Integer.parseInt(System.getProperty("server.port"));
            log.info("PORT=" + this.webPort + " found in command line parameter server.port");
        } else {
            this.webPort = dockerCompose.getPortForId(this.appId);
            log.info("PORT=" + this.webPort + " found in docker compose");
        }

        this.publicKey = dockerCompose.getPublicKeyForId(this.appId);
        this.userName = dockerCompose.getNameForId(this.appId);
        this.topicIndex = 0;

        if (publicKey.equalsIgnoreCase("not found") || publicKey.equalsIgnoreCase("not found")) {
            log.error("The chosen APP_ID must be present in the docker-compose config file. Exiting ...");
            System.exit(0);
        }

        for (Map.Entry<String, DockerService> service : dockerCompose.getServices().entrySet()) {
            DockerService dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().containsKey("APP_ID")) {
                    AppClient appClient = new AppClient();
                    appClient.setClientKey(dockerService.getEnvironment().get("PUBKEY"));
                    appClient.setClientName(dockerService.getContainer_name());
                    appClient.setPaymentAccountDetails(dockerService.getEnvironment().get("PAYMENT_ACCOUNT_DETAILS"));
                    appClient.setRoles(dockerService.getEnvironment().get("ROLES"));
                    appClient.setColor(dockerService.getEnvironment().get("COLOR"));
                    appClient.setAppId(Integer.parseInt(dockerService.getEnvironment().get("APP_ID")));
                    appClient.setWebPort(dockerService.getPortAsInteger());

                    this.appClients.add(appClient);
                }
            }
        }

    }

    public HCSCore getHCSCore() {
        return this.hcsCore;
    }

    public long getAppId() {
        return this.appId;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getTopicIndex() {
        return this.topicIndex;
    }

    public List<AppClient> getAppClients() {
        return this.appClients;
    }

    public int getWebPort() {
        return this.webPort;
    }
}
