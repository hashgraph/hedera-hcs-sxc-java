package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.hedera.hcsapp.appconfig.AppClient;
import com.hedera.hcsapp.appconfig.AppConfig;
import com.hedera.hcslib.HCSLib;

import io.github.cdimascio.dotenv.Dotenv;

public final class AppData {
    private HCSLib hcsLib;
    private int topicIndex = 0; // refers to the first topic ID in the config.yaml
    private String privateKey = "";
    private String publicKey = "";
    private String appName = "";
    private long appId = 0;
    private AppConfig appConfig;

    public AppData() throws FileNotFoundException, IOException {
        this.appConfig = new AppConfig();
        Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();

        this.appId = Long.parseLong(dotEnv.get("APPID"));
        this.hcsLib = new HCSLib(appId);
        this.privateKey = dotEnv.get("PK");
        this.publicKey = appConfig.getConfig().getAppClients().get((int)appId).getClientKey();
        this.appName = appConfig.getConfig().getAppClients().get((int)appId).getClientName();
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
    public String getAppName() {
        return this.appName;
    }
    public int getTopicIndex() {
        return this.topicIndex;
    }
    public List<AppClient> getAppClients() {
        return this.appConfig.getConfig().getAppClients();
    }
}
