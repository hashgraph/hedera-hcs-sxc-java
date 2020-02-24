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
import com.hedera.hcs.sxc.config.MirrorNode;
import com.hedera.hcs.sxc.config.Topic;
import com.hedera.hcs.sxc.config.YAMLConfig;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;

import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;
import com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface;
import com.hedera.hcs.sxc.plugins.Plugins;
import javax.crypto.KeyAgreement;
@Log4j2
public enum HCSCore { // singleton implementation
    
    INSTANCE();
    
    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
   
    private Map<AccountId, String> nodeMap = new HashMap<>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey operatorKey;
    private List<Topic> topics = new ArrayList<Topic>();
    private long maxTransactionFee = 0L;
    private String applicationId = "";
    private static SxcPersistence persistence;
    private boolean catchupHistory;
    private MessagePersistenceLevel messagePersistenceLevel;
    private String mirrorAddress;
    private Map<String, String> hibernateConfig = new HashMap<>();
    //private byte[] messageEncryptionKey = new byte[0];
    private YAMLConfig yamlConfig;
    private KeyAgreement tempKeyAgreement = null; // if set, user is KR initiator. 
    private Config config;
    private boolean initialised = false;
    private Dotenv dotEnv;
    private MirrorSubscriptionInterface mirrorSubscription;
   

    
    private  HCSCore() throws  ExceptionInInitializerError {  
    }
    
    private void init(String appId, String configFilePath, String environmentFilePath) throws Exception {    
        
        this.dotEnv = Dotenv.configure().filename(environmentFilePath).ignoreIfMissing().load();
        // optionally get operator key and account id from environment variables
        String envValue = getOptionalEnvValue("OPERATOR_KEY");
        if ( ! envValue.isEmpty()) {
            this.operatorKey = Ed25519PrivateKey.fromString(envValue);
        }
        envValue = getOptionalEnvValue("OPERATOR_ID");
        if ( ! envValue.isEmpty()) {
            this.operatorAccountId = AccountId.fromString(envValue);
        }
        
        try {    
            this.config = new Config(configFilePath);
            this.yamlConfig = config.getConfig();
        } catch (IOException ex) {
            log.error(ex);
            log.error("Can not load " + configFilePath);
            System.exit(0);
        }
        
        this.applicationId = appId;
        
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

        if(this.rotateKeys && !this.encryptMessages){
            log.error("config.yaml has key rotation enabled, however encryption is disabled. Exiting...");
            System.exit(0);
        }
        
        // replace hibernate configuration {appid}
        yamlConfig.getCoreHibernate().forEach((key,value) -> this.hibernateConfig.put(key, value.replace("{appid}",  this.applicationId)));  
        
        Class<?> persistenceClass = Plugins.find("com.hedera.hcs.sxc.plugin.persistence.*", "com.hedera.hcs.sxc.interfaces.SxcPersistence", true);
        
        
        this.setPersistence((SxcPersistence)persistenceClass.newInstance());
        this.getPersistence().setHibernateProperties(this.getHibernateConfig());
        
        
        // load mirror callback implementation at runtime
        Class<?> callbackClass = Plugins.find("com.hedera.hcs.sxc.plugin.mirror.*", "com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface", true);
        this.mirrorSubscription = ((MirrorSubscriptionInterface)callbackClass.newInstance());

        
        
        this.initialised = true;
    }
    
    
    
     
    public HCSCore singletonInstance() throws Exception{
        if( ! this.initialised) {
            init("", "./config/config.yaml", "./config/.env");
        }
        return INSTANCE;
    }
    
    /**
     * Init for HCS Core
     * @param applicationId - unique value per app instance using the component, if the app generates this value and stops/starts,
     * it must reuse the same applicationId to ensure consistent message delivery
     * @throws java.io.FileNotFoundException
     */
    
    public HCSCore singletonInstance(String appId) throws Exception{
        if( ! this.initialised) {
            init(appId, "./config/config.yaml", "./config/.env");
        }
        return INSTANCE;
    }

    /**
     * Init for HCS Core
     * @param applicationId - unique value per app instance using the component, if the app generates this value and stops/starts,
     * @param configFilePath - path to the configuration files
     * it must reuse the same applicationId to ensure consistent message delivery
     * FOR TESTING PURPOSES ONLY
     */
    public HCSCore singletonInstance(String appId, String configFilePath, String environmentFilePath) throws Exception {
        if( ! this.initialised) {
            init(appId,configFilePath, environmentFilePath);
        }
        return INSTANCE;
    }
    
    public HCSCore addAppParticipant(String appId, String theirEd25519PubKeyForSigning, String sharedSymmetricEncryptionKey){
        this.getPersistence().addAppParticipant(appId, theirEd25519PubKeyForSigning, sharedSymmetricEncryptionKey);
        return INSTANCE;
    }
    
    
    public HCSCore withMessageSignature(boolean signMessages) {
        this.signMessages = signMessages;
        return INSTANCE;
    }
    public HCSCore withEncryptedMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
        return INSTANCE;
    }
    //public HCSCore withMessageEncryptionKey(byte[] messageEncryptionKey) {
    //    this.messageEncryptionKey = messageEncryptionKey;
    //    return INSTANCE;
    //}
    public HCSCore withKeyRotation(boolean keyRotation, int frequency) {
        this.rotateKeys = keyRotation;
        this.rotationFrequency = frequency;
        return INSTANCE;
    }
    public HCSCore withNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return INSTANCE;
    }
    public HCSCore withOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return INSTANCE;
    }
    public HCSCore withOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.operatorKey = ed25519PrivateKey;
        return INSTANCE;
    }
    public HCSCore withTopicList(List<Topic> topics) {
        this.topics = topics;
        return INSTANCE;
    }
    public boolean getSignMessages() {
        return this.signMessages;
    }
    public boolean getEncryptMessages() {
        return this.encryptMessages;
    }
    //public byte[] getMessageEncryptionKey() {
    //    return this.messageEncryptionKey;
    //}
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
        return this.operatorKey;
    } 
    public List<Topic> getTopics() {
        return this.topics;
    }
    public long getMaxTransactionFee() {
        return this.maxTransactionFee;
    }
    public String getApplicationId() {
        return this.applicationId;
    }
    public String getMirrorAddress() {
        return this.mirrorAddress;
    }
    public boolean getCatchupHistory() {
        return this.catchupHistory;
    }
    public void setPersistence(SxcPersistence persistence) {
        HCSCore.persistence = persistence;
        HCSCore.persistence.setPersistenceLevel(this.messagePersistenceLevel);
    }

    public SxcPersistence getPersistence() {
        return HCSCore.persistence;
    }
    
    public Map<String, String> getHibernateConfig() {
        return this.hibernateConfig;
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

    //public void updateSecretKey(byte[] newSecretKey){
    //    this.messageEncryptionKey = newSecretKey;
    //}

    public MirrorSubscriptionInterface getMirrorSubscription() {
        return mirrorSubscription;
    }
    
    private String getOptionalEnvValue(String varName) throws Exception {
        String value = "";
        log.debug("Looking for " + varName + " in environment variables");
        if (System.getProperty(varName) != null) {
            value = System.getProperty(varName);
            log.debug(varName + " found in command line parameters");
        } else if ((this.dotEnv == null) || (this.dotEnv.get(varName) == null)) {
            value = "";
        } else {
            value = this.dotEnv.get(varName);
            log.debug(varName + " found in environment variables");
        }
        return value;
    }
    
}
