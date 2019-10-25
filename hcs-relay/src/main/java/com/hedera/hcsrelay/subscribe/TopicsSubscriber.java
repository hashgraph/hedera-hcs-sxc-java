package com.hedera.hcsrelay.subscribe;

import java.io.FileNotFoundException;
import java.io.IOException;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcsrelay.config.Config;

/**
 * Subscribes to topic(s) against a mirror node
 *
 */
public final class TopicsSubscriber {

    public TopicsSubscriber() throws FileNotFoundException, IOException {
        Config config = new Config();
        
        String mirrorAddress = config.getConfig().getMirrorAddress();
        
        for (TopicId topic : config.getConfig().getTopicIds()) {
            System.out.println("Subscribing to topic number " + topic.getTopicNum());
            // send subscription request to mirrorAddress for topic using the SDK
            // likely needs a callback method
        }
    }
}
