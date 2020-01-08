package com.hedera.hcsapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.dockercomposereader.DockerCompose;
import com.hedera.hcsapp.dockercomposereader.DockerComposeReader;
import com.hedera.hcsapp.dockercomposereader.Service;
import com.hedera.hcslib.HCSLib;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class AppData {
    private static HCSLib hcsLib;
    private int topicIndex = 0; // refers to the first topic ID in the config.yaml
    private String privateKey = "";
    private String publicKey = "";
    private String userName = "";
    private long appId = 0;
    List<AppClient> appClients = new ArrayList<>();

    public AppData() throws Exception {
        Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();

        if ((dotEnv.get("APP_ID") == null) || (dotEnv.get("APP_ID").isEmpty())) {
            // no environment variables found in environment or ./.env, try ./src/main/resource/.env
            dotEnv = Dotenv.configure().directory("./src/main/resources").ignoreIfMissing().load();
        }

        if ((dotEnv.get("APP_ID") == null) || (dotEnv.get("APP_ID").isEmpty())) {
            log.error("APPID environment variable is not set");
            log.error("APPID environment variable not found in ./.env");
            log.error("APPID environment variable not found in ./src/main/resources");
            throw new Exception("APPID environment variable is not set - exiting");
        }
        if ((dotEnv.get("OPERATOR_KEY") == null) || (dotEnv.get("OPERATOR_KEY").isEmpty())) {
            log.error("OPERATOR_KEY environment variable is not set");
            log.error("OPERATOR_KEY environment variable not found in ./.env");
            log.error("OPERATOR_KEY environment variable not found in ./src/main/resources");
            throw new Exception("OPERATOR_KEY environment variable is not set - exiting");
        }

        DockerCompose dockerCompose = DockerComposeReader.parse();

        this.appId = Long.parseLong(dotEnv.get("APP_ID"));
        AppData.hcsLib = new HCSLib(appId);
        this.privateKey = dotEnv.get("OPERATOR_KEY");
        this.publicKey = dockerCompose.getPublicKeyForId(this.appId);
        this.userName = dockerCompose.getNameForId(this.appId);
        this.topicIndex = 0;

        for (Map.Entry<String, Service> service : dockerCompose.getServices().entrySet()) {
            Service dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().containsKey("APP_ID")) {
                    AppClient appClient = new AppClient();
                    appClient.setClientKey(dockerService.getEnvironment().get("PUBKEY"));
                    appClient.setClientName(dockerService.getContainer_name());
                    appClient.setPaymentAccountDetails(dockerService.getEnvironment().get("PAYMENT_ACCOUNT_DETAILS"));
                    appClient.setRoles(dockerService.getEnvironment().get("ROLES"));

                    this.appClients.add(appClient);
                }
            }
        }

    }

    public HCSLib getHCSLib() {
        return AppData.hcsLib;
    }
    public long getAppId() {
        return this.appId;
    }
    public String getPrivateKey() {
        return this.privateKey;
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
}
