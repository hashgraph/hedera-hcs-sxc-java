package com.hedera.hcs.sxc.plugin.mirror.subscribe;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.Timestamp;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicResponse;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;

@TestInstance(Lifecycle.PER_CLASS)
public class MirrorTopicSubscriberTest {

    @Test
    public void testMirrorTopicSubscriberNoSubscribeFrom() throws Exception {
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        Optional<Instant> subscribeFrom = Optional.empty();
        MirrorTopicSubscriber mirrorTopicSubscriber = new MirrorTopicSubscriber(
                "mirror",
                8080,
                consensusTopicId, 
                subscribeFrom,
                null, 
                true);
        
        assertEquals("mirror", mirrorTopicSubscriber.getMirrorAddress());
        assertEquals(8080, mirrorTopicSubscriber.getMirrorPort());
        assertEquals(Optional.empty(), mirrorTopicSubscriber.getSubscribeFrom());
        assertEquals(1, mirrorTopicSubscriber.getTopicId().shard);
        assertEquals(2, mirrorTopicSubscriber.getTopicId().realm);
        assertEquals(3, mirrorTopicSubscriber.getTopicId().topic);

        assertDoesNotThrow(() -> {mirrorTopicSubscriber.subscribeForTest();});
    }        
    @Test
    public void testMirrorTopicSubscriberWithSubscribeFrom() throws Exception {
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        Instant timeNow = Instant.now();
        Optional<Instant> subscribeFrom = Optional.of(timeNow);

        final MirrorTopicSubscriber mirrorTopicSubscriber = new MirrorTopicSubscriber(
                "mirror",
                8080,
                consensusTopicId, 
                subscribeFrom,
                null, 
                true);
        
        assertEquals("mirror", mirrorTopicSubscriber.getMirrorAddress());
        assertEquals(8080, mirrorTopicSubscriber.getMirrorPort());
        assertEquals(subscribeFrom, mirrorTopicSubscriber.getSubscribeFrom());
        assertEquals(1, mirrorTopicSubscriber.getTopicId().shard);
        assertEquals(2, mirrorTopicSubscriber.getTopicId().realm);
        assertEquals(3, mirrorTopicSubscriber.getTopicId().topic);
        
        assertDoesNotThrow(() -> {mirrorTopicSubscriber.subscribeForTest();});
    }
    
    @Test
    public void testMirrorTopicOnMessage() throws Exception {
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        Instant timeNow = Instant.now();
        Optional<Instant> subscribeFrom = Optional.of(timeNow);

        final MirrorTopicSubscriber mirrorTopicSubscriber = new MirrorTopicSubscriber(
                "mirror",
                8080,
                consensusTopicId, 
                subscribeFrom,
                null, 
                true);
        
        assertEquals("mirror", mirrorTopicSubscriber.getMirrorAddress());
        assertEquals(8080, mirrorTopicSubscriber.getMirrorPort());
        assertEquals(subscribeFrom, mirrorTopicSubscriber.getSubscribeFrom());
        assertEquals(1, mirrorTopicSubscriber.getTopicId().shard);
        assertEquals(2, mirrorTopicSubscriber.getTopicId().realm);
        assertEquals(3, mirrorTopicSubscriber.getTopicId().topic);
        
        mirrorTopicSubscriber.subscribeForTest();
        
        // invalid message type
        ConsensusTopicResponse proto = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(100).setNanos(10).build())
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(10L)
                .build();
        MirrorConsensusTopicResponse response = Mockito.mock(MirrorConsensusTopicResponse.class, Mockito.withSettings().useConstructor(proto));
        HCSCallBackFromMirror hcsCallbackFromMirror = Mockito.mock(HCSCallBackFromMirror.class);
        
        assertThrows(com.google.protobuf.InvalidProtocolBufferException.InvalidWireTypeException.class, 
            () -> {mirrorTopicSubscriber.onMirrorMessage(response, hcsCallbackFromMirror, consensusTopicId);}
        );
        
        // valid message type
        ApplicationMessageID applicationMessageID = ApplicationMessageID.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(1).build())
                .setValidStart(com.hedera.hcs.sxc.proto.Timestamp.newBuilder().setSeconds(100).setNanos(10).build())
                .build();
        ApplicationMessageChunk applicationMessageChunk = ApplicationMessageChunk.newBuilder()
                .setApplicationMessageId(applicationMessageID)
                .setChunkIndex(1)
                .setChunksCount(3)
                .setMessageChunk(ByteString.copyFromUtf8("chunk"))
                .build();
        proto = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(100).setNanos(10).build())
                .setMessage(applicationMessageChunk.toByteString())
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(10L)
                .build();
        
        MirrorConsensusTopicResponse response2 = Mockito.mock(MirrorConsensusTopicResponse.class, Mockito.withSettings().useConstructor(proto));
        assertDoesNotThrow(() -> {mirrorTopicSubscriber.onMirrorMessage(response2, hcsCallbackFromMirror, consensusTopicId);});

    }
}
