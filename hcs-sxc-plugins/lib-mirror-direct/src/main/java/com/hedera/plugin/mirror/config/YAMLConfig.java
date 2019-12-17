package com.hedera.plugin.mirror.config;

import java.util.ArrayList;
import java.util.List;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

public final class YAMLConfig {

    private String mirrorAddress = "";
    private List<Topic> topics = new ArrayList<Topic>();
    
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