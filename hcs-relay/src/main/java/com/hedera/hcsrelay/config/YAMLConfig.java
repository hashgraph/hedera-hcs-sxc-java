package com.hedera.hcsrelay.config;

import java.util.ArrayList;
import java.util.List;
import com.hedera.hashgraph.sdk.consensus.TopicId;

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
    public List<TopicId> getTopicIds() {
        List<TopicId> topicIds = new ArrayList<TopicId>();
        
        for (Topic topic : this.topics) {
            topicIds.add(TopicId.fromString(topic.getTopic()));
        }
        
        return topicIds;
    }
}