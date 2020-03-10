package com.hedera.hcs.sxc.commonobjects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.Timestamp;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

public class SxcConsensusMessageTest {
    @Test
    public void testSxcConsensusMessageConstructorTopic() {
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(100).setNanos(10).build();
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(timestamp)
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(20)
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage(consensusTopicId, consensusTopicResponse);
        
        assertEquals(timestamp.getSeconds(), sxcConsensusMessage.consensusTimestamp.getEpochSecond());
        assertEquals(timestamp.getNanos(), sxcConsensusMessage.consensusTimestamp.getNano());
        assertArrayEquals("message".getBytes(), sxcConsensusMessage.message);
        assertArrayEquals("runninghash".getBytes(), sxcConsensusMessage.runningHash);
        assertEquals(20, sxcConsensusMessage.sequenceNumber);
        assertEquals(consensusTopicId.shard, sxcConsensusMessage.topicId.shard);
        assertEquals(consensusTopicId.realm, sxcConsensusMessage.topicId.realm);
        assertEquals(consensusTopicId.topic, sxcConsensusMessage.topicId.topic);
    }
    
    @Test
    public void testSxcConsensusMessageConstructorString() {
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(100).setNanos(10).build();
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(timestamp)
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(20)
                .build();
        
        SxcConsensusMessage sxcConsensusMessage = new SxcConsensusMessage("1.2.3", consensusTopicResponse);
        
        assertEquals(timestamp.getSeconds(), sxcConsensusMessage.consensusTimestamp.getEpochSecond());
        assertEquals(timestamp.getNanos(), sxcConsensusMessage.consensusTimestamp.getNano());
        assertArrayEquals("message".getBytes(), sxcConsensusMessage.message);
        assertArrayEquals("runninghash".getBytes(), sxcConsensusMessage.runningHash);
        assertEquals(20, sxcConsensusMessage.sequenceNumber);
        assertEquals(1, sxcConsensusMessage.topicId.shard);
        assertEquals(2, sxcConsensusMessage.topicId.realm);
        assertEquals(3, sxcConsensusMessage.topicId.topic);
        
        assertEquals("message", sxcConsensusMessage.getMessageString());
        
        String toString = "ConsensusMessage{topicId=1.2.3, consensusTimestamp=1970-01-01T00:01:40.000000010Z, message=[109, 101, 115, 115, 97, 103, 101], runningHash=[114, 117, 110, 110, 105, 110, 103, 104, 97, 115, 104], sequenceNumber=20}";
        assertEquals(toString, sxcConsensusMessage.toString());
    }

}
