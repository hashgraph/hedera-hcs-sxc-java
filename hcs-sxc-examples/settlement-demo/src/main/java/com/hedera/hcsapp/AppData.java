package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.appconfig.AppConfig;
import com.hedera.hcslib.HCSLib;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class AppData {
    private HCSLib hcsLib;
    private int topicIndex = 0; // refers to the first topic ID in the config.yaml
    private String privateKey = "";
    private String publicKey = "";
    private String userName = "";
    private long appId = 0;
    private AppConfig appConfig;

    public AppData() throws FileNotFoundException, IOException {
        this.appConfig = new AppConfig();
        Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();

        if (dotEnv.get("APP_ID").isEmpty()) {
            log.error("APPID environment variable is not set - exiting");
            System.exit(0);
        }

        this.appId = Long.parseLong(dotEnv.get("APP_ID"));
        this.hcsLib = new HCSLib(appId);
        this.privateKey = dotEnv.get("PK");
        this.publicKey = appConfig.getConfig().getAppClients().get((int)appId).getClientKey();
        this.userName = appConfig.getConfig().getAppClients().get((int)appId).getClientName();
        this.topicIndex = 0;
    }
    
    public HCSLib getHCSLib() { 
        return this.hcsLib;
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
        return this.appConfig.getConfig().getAppClients();
    }
}
