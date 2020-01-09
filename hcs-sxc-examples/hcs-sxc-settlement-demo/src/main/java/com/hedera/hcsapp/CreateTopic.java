package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.consensus.CreateHCSTopic;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CreateTopic {

    public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException {
        HCSCore hcsCore = new HCSCore(0L);
        // create topics on HCS
        CreateHCSTopic createHCSTopic = new CreateHCSTopic(hcsCore);
        ConsensusTopicId topicId = createHCSTopic.execute();
        log.info(topicId.toString());
    }

}
