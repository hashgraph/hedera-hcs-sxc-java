package com.hedera.hcs.sxc.config;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

public final class Topic {
    private String topic = "";
    private long shard = 0;
    private long realm = 0;
    private long num = 0;
    
    public String getTopic() {
        return this.topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
        String[] ids = this.topic.split("\\.");
        shard = Long.parseLong(ids[0]);
        realm = Long.parseLong(ids[1]);
        num = Long.parseLong(ids[2]);
    }
    public ConsensusTopicId getConsensusTopicId() {
        return new ConsensusTopicId(shard, realm, num);
    }
}
