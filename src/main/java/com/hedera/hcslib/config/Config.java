package com.hedera.hcslib.config;

import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import io.github.cdimascio.dotenv.Dotenv;

public final class Config {
    
    private Dotenv dotEnv;
    
    public Config() {
        Dotenv.configure().ignoreIfMissing();
        this.dotEnv = Dotenv.load();        
    }
 
    public Ed25519PrivateKey getOperatorKey() {
        return Ed25519PrivateKey.fromString(dotEnv.get("OPERATOR_KEY"));
    }
    public String getOperatorAccountId() {
        return dotEnv.get("OPERATOR_ID");
    }
    public Map<AccountId, String> getNodesMap() {
        
        String nodes = dotEnv.get("NODES");
        Map<AccountId, String> nodeList = new HashMap<AccountId, String>();
        
        String[] nodeData = nodes.split(",");
        for (int i=0; i < nodeData.length; i++) {
            nodeList.put(AccountId.fromString(nodeData[i+1]), nodeData[i]);
            i++;
        }
        
        return nodeList;
    }
}
