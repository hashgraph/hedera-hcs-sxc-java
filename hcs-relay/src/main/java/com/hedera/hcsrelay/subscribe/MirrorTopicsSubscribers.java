package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcsrelay.config.Config;

/**
 * Subscribes to topic(s) against a mirror node
 *
 */
public final class MirrorTopicsSubscribers {

    public MirrorTopicsSubscribers(String nodeAddress, int nodePort, Config config) throws Exception {
        for (TopicId topic : config.getConfig().getTopicIds()) {
            System.out.println("Subscribing to topic number " + topic.getTopicNum());
            // subscribe to topic with mirror node
            MirrorTopicSubscriber subscriber = new MirrorTopicSubscriber(nodeAddress, nodePort, topic);
            Thread subscriberThread = new Thread(subscriber);
            subscriberThread.start();
        }
    }
    
}
