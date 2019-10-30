package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcsrelay.config.Config;

/**
 * Subscribes to topic(s) against a mirror node
 * and sets up topics on mq
 */
public final class MirrorTopicsSubscribers {

    public MirrorTopicsSubscribers(String nodeAddress, int nodePort, Config config) throws Exception {
        System.out.println("Relay topics to subscribe to from mirror and queue");
        for (TopicId topic : config.getConfig().getTopicIds()) {
            System.out.println("Processing topic num: " + topic.getTopicNum());
            // create queue topic
            boolean blockingSetupJmsTopic = QueueTopicOperations.blockingCreateJmsTopic(config,  topic.getTopicNum());
            if (blockingSetupJmsTopic) {
                System.out.println("Queue is setup for topic num " + topic.getTopicNum());
            
                // subscribe to topic with mirror node
                MirrorTopicSubscriber subscriber = new MirrorTopicSubscriber(nodeAddress, nodePort, topic);
                Thread subscriberThread = new Thread(subscriber);
                subscriberThread.start();
            } else {
                throw new Exception("Queue topic subscription error");
            }
        }
    }
    
}
