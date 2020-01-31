package com.hedera.hcs.sxc.config;

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

import java.util.ArrayList;
import java.util.List;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;

public final class AppNet {
    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotateKeyFrequency = 0;
    private MessagePersistenceLevel persistenceLevel = MessagePersistenceLevel.NONE;
    private List<Topic> topics = new ArrayList<Topic>();
    private boolean catchupHistory = false;

    public boolean getCatchupHistory() {
        return this.catchupHistory;
    }
    public void setCatchupHistory(boolean catchupHistory) {
        this.catchupHistory = catchupHistory;
    }
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
    public MessagePersistenceLevel getPersistenceLevel() {
        return persistenceLevel;
    }

    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel) {
        this.persistenceLevel = persistenceLevel;
    }

    
}
