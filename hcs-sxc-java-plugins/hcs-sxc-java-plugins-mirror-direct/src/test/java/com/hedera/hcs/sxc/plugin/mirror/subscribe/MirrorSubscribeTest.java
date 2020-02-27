package com.hedera.hcs.sxc.plugin.mirror.subscribe;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

public class MirrorSubscribeTest {

    @Test
    public void testMirrorSubscribe() throws Exception {
        MirrorSubscribe mirrorSubscribe = new MirrorSubscribe();
        mirrorSubscribe.setTestMode(true);
        List<ConsensusTopicId> topicIds = new ArrayList<ConsensusTopicId>();
        
        ConsensusTopicId consensusTopicId = new ConsensusTopicId(1, 2, 3);
        topicIds.add(consensusTopicId);
         
        assertThrows(Exception.class, () -> {mirrorSubscribe.init(null, "1234", null, "mirrorAddress", topicIds);});
        assertDoesNotThrow(() -> {mirrorSubscribe.init(null, "1234", null, "mirrorAddress:2020", topicIds);});
    }
}
