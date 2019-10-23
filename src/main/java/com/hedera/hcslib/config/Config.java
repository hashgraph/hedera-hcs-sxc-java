package com.hedera.hcslib.config;

import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import io.github.cdimascio.dotenv.Dotenv;

public final class Config {
    
    /** 
     * Manages the readonly configuration data 
     */
    
    private Dotenv dotEnv;

    public Config() {
        Dotenv.configure().ignoreIfMissing();
        this.dotEnv = Dotenv.load();        
    }

    /** 
     * Returns an Ed25519PrivateKey from the OPERATOR_KEY environment variable
     * @return Ed25519PrivateKey
     */
    public Ed25519PrivateKey getOperatorKey() {
        return Ed25519PrivateKey.fromString(dotEnv.get("OPERATOR_KEY"));
    }

    /** 
     * Returns a string representing the value of the OPERATOR_ID environment variable
     * @return String
     */
    public String getOperatorAccount() {
        return dotEnv.get("OPERATOR_ID");
    }

    /** 
     * Returns a string representing the value of the OPERATOR_ID environment variable
     * @return AccountId
     */
    public AccountId getOperatorAccountId() {
        return AccountId.fromString(dotEnv.get("OPERATOR_ID"));
    }

    /** 
     * Returns an AccountId representing the value of the OPERATOR_ID environment variable
     * @return AccountId
     */
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
