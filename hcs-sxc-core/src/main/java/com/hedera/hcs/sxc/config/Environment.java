package com.hedera.hcs.sxc.config;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import io.github.cdimascio.dotenv.Dotenv;

public final class Environment {
    
    /** 
     * Manages configuration data which is either held in environment variables or a .env file 
     */
    
    private Dotenv dotEnv;

    public Environment() {
        this.dotEnv = Dotenv.configure().ignoreIfMissing().load();        
    }
    public Environment(String fileName) {
        this.dotEnv = Dotenv.configure().filename(fileName).ignoreIfMissing().load();        
    }

    /** 
     * Returns an Ed25519PrivateKey from the OPERATOR_KEY environment variable
     * @return Ed25519PrivateKey
     */
    public Ed25519PrivateKey getOperatorKey() {
        String operatorKey = dotEnv.get("OPERATOR_KEY");
        if (operatorKey == null){
            operatorKey = System.getProperty("OPERATOR_KEY");
        }   
        return Ed25519PrivateKey.fromString(operatorKey);
        
        
    }

    /** 
     * Returns a string representing the value of the OPERATOR_ID environment variable
     * @return String
     */
    public String getOperatorAccount() {
        String operatorId = dotEnv.get("OPERATOR_ID");
        if (operatorId == null){
            operatorId = System.getProperty("OPERATOR_ID");
        }   
        return operatorId;
    }

    /** 
     * Returns a string representing the value of the OPERATOR_ID environment variable
     * @return AccountId
     */
    public AccountId getOperatorAccountId() {
        String operatorId = dotEnv.get("OPERATOR_ID");
        if (operatorId == null){
            operatorId = System.getProperty("OPERATOR_ID");
        }
        return AccountId.fromString(operatorId);
    }
    
    /** 
     * Returns the app id
     */
    public int getAppId() {
        String appId = dotEnv.get("APP_ID");
        if(appId == null){
            appId = System.getProperty("APP_ID");
        }
        return Integer.parseInt(appId);
    }
}
