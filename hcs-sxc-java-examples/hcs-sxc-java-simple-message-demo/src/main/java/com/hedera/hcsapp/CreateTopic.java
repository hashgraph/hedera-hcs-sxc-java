package com.hedera.hcsapp;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.consensus.CreateHCSTopic;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CreateTopic {

    public static void main(String[] args) throws Exception {
        HCSCore hcsCore = HCSCore.INSTANCE.getInstance();
        // create topics on HCS
        CreateHCSTopic createHCSTopic = new CreateHCSTopic(hcsCore);
        ConsensusTopicId topicId = createHCSTopic.execute();
        log.info(topicId.topic);
    }

}
