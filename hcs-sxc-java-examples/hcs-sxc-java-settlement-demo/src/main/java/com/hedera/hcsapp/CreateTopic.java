package com.hedera.hcsapp;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.consensus.CreateHCSTopic;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CreateTopic {

    public static void main(String[] args) throws Exception {
        HCSCore hcsCore = new HCSCore(0L);
        // create topics on HCS
        ConsensusTopicId topicId = new CreateHCSTopic(hcsCore)
                .execute();
        log.info(topicId.toString());
    }

}
