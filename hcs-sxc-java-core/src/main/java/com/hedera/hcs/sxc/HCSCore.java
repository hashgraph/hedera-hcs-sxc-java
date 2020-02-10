package com.hedera.hcs.sxc;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.config.AppNet;
import com.hedera.hcs.sxc.config.Config;
import com.hedera.hcs.sxc.config.Environment;
import com.hedera.hcs.sxc.config.MirrorNode;
import com.hedera.hcs.sxc.config.Topic;
import com.hedera.hcs.sxc.config.YAMLConfig;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;

import io.github.cdimascio.dotenv.Dotenv;

import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyAgreement;

public enum HCSCore { // singleton implementation
    
    INSTANCE();
    
    

    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
   
    private Map<AccountId, String> nodeMap = new HashMap<>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey;
    private List<Topic> topics = new ArrayList<Topic>();
    private long maxTransactionFee = 0L;
    private long applicationId = -1L;
    private static SxcMessagePersistence persistence;
    private boolean catchupHistory;
    private MessagePersistenceLevel messagePersistenceLevel;
    private String mirrorAddress;
    private Map<String, String> hibernateConfig = new HashMap<>();
    private byte[] messageEncryptionKey = new byte[0];
    private Environment environment = new Environment();
    private YAMLConfig yamlConfig;
    private KeyAgreement tempKeyAgreement = null; // if set, user is KR initiator. 
    private Config config;
    
    /**
     * Constructor for HCS Core
     * @param applicationId - unique value per app instance using the component, if the app generates this value and stops/starts,
     * it must reuse the same applicationId to ensure consistent message delivery
     * @throws java.io.FileNotFoundException
     */
    /**
     * Constructor for HCS Core
     * @param applicationId - unique value per app instance using the component, if the app generates this value and stops/starts,
     * @param configFilePath - path to the configuration files
     * it must reuse the same applicationId to ensure consistent message delivery
     * FOR TESTING PURPOSES ONLY
     */
    
    
    private  HCSCore() throws  ExceptionInInitializerError {  
    }
    
    private void init(long appId, String configFilePath, String environmentFilePath) {
       
        
        try {    
            this.environment = new Environment(environmentFilePath);
            this.config = new Config(configFilePath);
            this.yamlConfig = config.getConfig();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Can not load one of " + environmentFilePath + ", " + configFilePath);
            System.exit(0);
        }
        
        if (this.applicationId == -1){
            this.applicationId = this.environment.getAppId();
        }
        else { 
            this.applicationId  = appId;
        }
        
        this.nodeMap = yamlConfig.getNodesMap();
        this.maxTransactionFee = yamlConfig.getHCSTransactionFee();

        MirrorNode mirrorNode = yamlConfig.getMirrorNode();
        this.mirrorAddress = mirrorNode.getAddress();
        
        AppNet appnet = yamlConfig.getAppNet();
        this.signMessages = appnet.getSignMessages();
        this.encryptMessages = appnet.getEncryptMessages();
        this.rotateKeys = appnet.getRotateKeys();
        this.topics = appnet.getTopics();
        this.catchupHistory = appnet.getCatchupHistory();
        this.messagePersistenceLevel = appnet.getPersistenceLevel();

        this.operatorAccountId = this.environment.getOperatorAccountId();
        this.ed25519PrivateKey = this.environment.getOperatorKey();
        if(this.encryptMessages){
            this.messageEncryptionKey  = this.environment.getMessageEncryptionKey();
        }
        if(this.rotateKeys && !this.encryptMessages){
            System.out.println("config.ini has key rotation enabled, however encryption is disabled. Exiting...");
            System.exit(0);
        }
        
        
        // replace hibernate configuration {appid}
        yamlConfig.getCoreHibernate().forEach((key,value) -> this.hibernateConfig.put(key, value.replace("{appid}",  Long.toString(this.applicationId))));
    }
    
    
    public HCSCore singletonInstanceDefault(long appId){
        if(this.applicationId==-1) init(appId, "./config/config.yaml", "./config/.env");
        return INSTANCE;
    }

    public HCSCore singletonInstanceWithAppIdEnvAndConfig (long appId, String configFilePath, String environmentFilePath) {
        if(this.applicationId==-1) init(appId,configFilePath, environmentFilePath);
        return INSTANCE;
    }
    
    
    public HCSCore withMessageSignature(boolean signMessages) {
        this.signMessages = signMessages;
        return this;
    }
    public HCSCore withEncryptedMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
        return this;
    }
    public HCSCore withMessageEncryptionKey(byte[] messageEncryptionKey) {
        this.messageEncryptionKey = messageEncryptionKey;
        return this;
    }
    public HCSCore withKeyRotation(boolean keyRotation, int frequency) {
        this.rotateKeys = keyRotation;
        this.rotationFrequency = frequency;
        return this;
    }
    public HCSCore withNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }
    public HCSCore withOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }
    public HCSCore withOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }
    public HCSCore withTopicList(List<Topic> topics) {
        this.topics = topics;
        return this;
    }
    public boolean getSignMessages() {
        return this.signMessages;
    }
    public boolean getEncryptMessages() {
        return this.encryptMessages;
    }
    public byte[] getMessageEncryptionKey() {
        return this.messageEncryptionKey;
    }
    public boolean getRotateKeys() {
        return this.rotateKeys;
    }
    public int getRotationFrequency() {
        return this.rotationFrequency;
    }
    public Map<AccountId, String> getNodeMap() {
        return this.nodeMap;
    }
    public AccountId getOperatorAccountId() {
        return this.operatorAccountId;
    } 
    public Ed25519PrivateKey getEd25519PrivateKey() {
        return this.ed25519PrivateKey;
    } 
    public List<Topic> getTopics() {
        return this.topics;
    }
    public long getMaxTransactionFee() {
        return this.maxTransactionFee;
    }
    public long getApplicationId() {
        return this.applicationId;
    }
    public String getMirrorAddress() {
        return this.mirrorAddress;
    }
    public boolean getCatchupHistory() {
        return this.catchupHistory;
    }
    public void setMessagePersistence(SxcMessagePersistence persistence) {
        HCSCore.persistence = persistence;
        HCSCore.persistence.setPersistenceLevel(this.messagePersistenceLevel);
    }

    public SxcMessagePersistence getMessagePersistence() {
        return HCSCore.persistence;
    }
    
    public Map<String, String> getHibernateConfig() {
        return this.hibernateConfig;
    }
    
    public Dotenv getEnvironment() {
        return this.environment.getDotEnv();
    }
    public List<ConsensusTopicId> getConsensusTopicIds() {
        List<ConsensusTopicId> consensusTopicIds = new ArrayList<ConsensusTopicId>();
        for (Topic topic : this.topics) {
            consensusTopicIds.add(topic.getConsensusTopicId());
        }
        return consensusTopicIds;
    }

    public KeyAgreement getTempKeyAgreement() {
        return tempKeyAgreement;
    }

    public void setTempKeyAgreement(KeyAgreement tempKeyAgreement) {
        this.tempKeyAgreement = tempKeyAgreement;
    }

    public void updateSecretKey(byte[] newSecretKey){
        this.messageEncryptionKey = newSecretKey;
    }
    
    
    
    
}
