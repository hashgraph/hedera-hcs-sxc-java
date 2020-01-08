package com.hedera.hcslib.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

public class AbstractConfigTest {
    protected final void assertTopicId(long shardNum, long realmNum, long topicNum, ConsensusTopicId topicId) {
        assertAll(
                () -> assertEquals (shardNum, topicId.shard)
                ,() -> assertEquals (realmNum, topicId.realm)
                ,() -> assertEquals (topicNum, topicId.topic)
         );
    }

}
