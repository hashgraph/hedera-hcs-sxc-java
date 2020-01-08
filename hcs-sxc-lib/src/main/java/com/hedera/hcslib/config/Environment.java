package com.hedera.hcslib.config;

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
     * Returns the app id
     */
    public int getAppId() {
        return Integer.parseInt(dotEnv.get("APP_ID"));
    }
}
