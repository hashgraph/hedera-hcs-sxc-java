package com.hedera.plugin.persistence.config;

import java.util.ArrayList;
import java.util.List;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcslib.interfaces.MessagePersistenceLevel;

public final class AppNet {
    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotateKeyFrequency = 0;
    private MessagePersistenceLevel persistenceLevel = MessagePersistenceLevel.NONE;
    private List<Topic> topics = new ArrayList<Topic>();

    public boolean getSignMessages() {
        return signMessages;
    }
    public void setSignMessages(boolean signMessages) {
        this.signMessages = signMessages;
    }
    public boolean getEncryptMessages() {
        return encryptMessages;
    }
    public void setEncryptMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
    }
    public boolean getRotateKeys() {
        return this.rotateKeys;
    }
    public void setRotateKeys(boolean rotateKeys) {
        this.rotateKeys = rotateKeys;
    }
    public int getRotateKeyFrequency() {
        return this.rotateKeyFrequency;
    }
    public void setRotateKeyFrequency(int rotateKeyFrequency) {
        this.rotateKeyFrequency = rotateKeyFrequency;
    }
    public List<Topic> getTopics() {
        return this.topics;
    }
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
    /** 
     * Returns a list of TopicIds
     * @return List<TopicId> 
     */
    public List<TopicId> getTopicIds() {
        List<TopicId> topicIds = new ArrayList<TopicId>();
        
        for (Topic topic : this.topics) {
            topicIds.add(TopicId.fromString(topic.getTopic()));
        }
        
        return topicIds;
    }

    public MessagePersistenceLevel getPersistenceLevel() {
        return persistenceLevel;
    }

    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel) {
        this.persistenceLevel = persistenceLevel;
    }

    
}
