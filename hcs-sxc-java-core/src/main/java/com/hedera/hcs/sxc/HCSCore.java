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

public final class HCSCore {

    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey;
    private List<Topic> topics = new ArrayList<Topic>();
    private long maxTransactionFee = 0;
    private long applicationId = 0;
    private static SxcMessagePersistence persistence;
    private boolean catchupHistory;
    private MessagePersistenceLevel messagePersistenceLevel;
    private String mirrorAddress;
    private Map<String, String> hibernateConfig = new HashMap<String, String>();
    private Environment environment = new Environment();
    
    /**
     * Constructor for HCS Core
     * @param applicationId - unique value per app instance using the component, if the app generates this value and stops/starts,
     * it must reuse the same applicationId to ensure consistent message delivery
     */
    public HCSCore(long applicationId) throws FileNotFoundException, IOException {
        Config config = new Config();
        YAMLConfig yamlConfig = config.getConfig();

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

        this.applicationId = applicationId;
        
        String appId = Long.toString(this.applicationId);
        // replace hibernate configuration {appid}
        yamlConfig.getCoreHibernate().forEach((key,value) -> this.hibernateConfig.put(key, value.replace("{appid}", appId))); 
    }
    public HCSCore withMessageSignature(boolean signMessages) {
        this.signMessages = signMessages;
        return this;
    }
    public HCSCore withEncryptedMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
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
}
