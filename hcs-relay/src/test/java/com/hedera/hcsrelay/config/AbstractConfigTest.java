package com.hedera.hcsrelay.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.hashgraph.sdk.consensus.TopicId;

public class AbstractConfigTest {
    protected final void assertTopicId(long shardNum, long realmNum, long topicNum, TopicId topicId) {
        assertAll(
                () -> assertEquals (shardNum, topicId.getShardNum())
                ,() -> assertEquals (realmNum, topicId.getRealmNum())
                ,() -> assertEquals (topicNum, topicId.getTopicNum())
         );
    }

}
