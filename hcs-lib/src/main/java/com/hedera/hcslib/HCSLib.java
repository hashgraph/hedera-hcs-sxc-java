package com.hedera.hcslib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcslib.config.AppNet;
import com.hedera.hcslib.config.Config;
import com.hedera.hcslib.config.Environment;
import com.hedera.hcslib.config.MirrorNode;
import com.hedera.hcslib.config.YAMLConfig;
import com.hedera.hcslib.interfaces.LibMessagePersistence;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;

public final class HCSLib {

    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey;
    private List<ConsensusTopicId> topicIds = new ArrayList<ConsensusTopicId>();
    private long hcsTransactionFee = 0;
    private long applicationId = 0;
    private static LibMessagePersistence persistence;
    private boolean catchupHistory;
    private MessagePersistenceLevel messagePersistenceLevel;
    private String mirrorAddress;
    private int reconnectDelay = 0;

    /**
     * Constructor for HCS Lib
     * @param applicationId - unique value per app instance using the library, if the app generates this value and stops/starts,
     * it must reuse the same applicationId to ensure consistent message delivery
     */
    public HCSLib(long applicationId) throws FileNotFoundException, IOException {
        Config config = new Config();
        YAMLConfig yamlConfig = config.getConfig();
        Environment environment = new Environment();

        this.nodeMap = yamlConfig.getNodesMap();
        this.hcsTransactionFee = yamlConfig.getHCSTransactionFee();

        MirrorNode mirrorNode = yamlConfig.getMirrorNode();
        this.mirrorAddress = mirrorNode.getAddress();
        this.reconnectDelay = mirrorNode.getReconnectDelay();
        
        AppNet appnet = yamlConfig.getAppNet();
        this.signMessages = appnet.getSignMessages();
        this.encryptMessages = appnet.getEncryptMessages();
        this.rotateKeys = appnet.getRotateKeys();
        this.topicIds = appnet.getTopicIds();
        this.catchupHistory = appnet.getCatchupHistory();
        this.messagePersistenceLevel = appnet.getPersistenceLevel();

        this.operatorAccountId = environment.getOperatorAccountId();
        this.ed25519PrivateKey = environment.getOperatorKey();

        this.applicationId = applicationId;
    }
    public HCSLib withMessageSignature(boolean signMessages) {
        this.signMessages = signMessages;
        return this;
    }
    public HCSLib withEncryptedMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
        return this;
    }
    public HCSLib withKeyRotation(boolean keyRotation, int frequency) {
        this.rotateKeys = keyRotation;
        this.rotationFrequency = frequency;
        return this;
    }
    public HCSLib withNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }
    public HCSLib withOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }
    public HCSLib withOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }
    public HCSLib withTopicList(List<ConsensusTopicId> topicIds) {
        this.topicIds = topicIds;
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
    public List<ConsensusTopicId> getTopicIds() {
        return this.topicIds;
    }
    public long getHCSTransactionFee() {
        return this.hcsTransactionFee;
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
    public int getMirrorReconnectDelay() {
        return this.reconnectDelay;
    }
    public void setMessagePersistence(LibMessagePersistence persistence) {
        HCSLib.persistence = persistence;
        HCSLib.persistence.setPersistenceLevel(this.messagePersistenceLevel);
    }

    public LibMessagePersistence getMessagePersistence() {
        return HCSLib.persistence;
    }
}
