package com.hedera.hcs.sxc.relay.config;

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

public final class YAMLConfig {

    private String mirrorAddress = "";
    private List<Topic> topics = new ArrayList<Topic>();
    private Queue queue = new Queue();
    private boolean catchupHistory;
    private String lastConsensusTimeFile;
    
    public String getLastConsensusTimeFile() {
        return this.lastConsensusTimeFile;
    }
    public void setLastConsensusTimeFile(String lastConsensusTimeFile) {
        this.lastConsensusTimeFile = lastConsensusTimeFile;
    }
    public boolean getCatchupHistory() {
        return this.catchupHistory;
    }
    public void setCatchupHistory(boolean catchupHistory) {
        this.catchupHistory = catchupHistory;
    }
    
    public String getMirrorAddress() {
        return this.mirrorAddress;
    }
    public void setMirrorAddress(String mirrorAddress) {
        this.mirrorAddress = mirrorAddress;
    }
    public List<Topic> getTopics() {
        return this.topics;
    }
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
    public Queue getQueue() {
        return this.queue;
    }
    public void setQueue(Queue queue) {
        this.queue = queue;
    }
    /** 
     * Returns a list of TopicIds
     * @return List<TopicId> 
     */
    public List<ConsensusTopicId> getTopicIds() {
        List<ConsensusTopicId> topicIds = new ArrayList<ConsensusTopicId>();
        
        for (Topic topic : this.topics) {
            String[] ids = topic.getTopic().split("\\.");
            topicIds.add(new ConsensusTopicId(Long.parseLong(ids[0]), Long.parseLong(ids[1]), Long.parseLong(ids[2])));
        }
        
        return topicIds;
    }
}
