package com.hedera.hcsapp;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.consensus.CreateHCSTopic;

public class CreateTopic {

    public static void main(String[] args) throws FileNotFoundException, IOException, HederaNetworkException, IllegalArgumentException, HederaException {
        HCSLib hcsLib = new HCSLib();
        // create topics on HCS
        CreateHCSTopic createHCSTopic = new CreateHCSTopic(hcsLib);
        TopicId topicId = createHCSTopic.execute();
        System.out.println(topicId.getTopicNum());
    }

}
