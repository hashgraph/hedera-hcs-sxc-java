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

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

public final class Topic {
    private String topic = "";
    private long shard = 0;
    private long realm = 0;
    private long num = 0;
    private String submitKey = "";
    
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
    public void setSubmitKey(String submitKey) {
        this.submitKey = submitKey;
    }
    public String getSubmitKey() {
        return this.submitKey;
    }
}
