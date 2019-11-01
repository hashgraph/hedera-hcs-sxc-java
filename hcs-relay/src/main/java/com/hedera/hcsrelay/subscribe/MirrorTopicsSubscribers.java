package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcsrelay.config.Config;

import lombok.extern.log4j.Log4j2;

/**
 * Subscribes to topic(s) against a mirror node and sets up topics on mq
 */
@Log4j2
public final class MirrorTopicsSubscribers {

    public MirrorTopicsSubscribers(String nodeAddress, int nodePort, Config config) throws Exception {
        log.info("Relay topics to subscribe to from mirror and queue");
        boolean blockingSetupJmsTopic = QueueTopicOperations.blockingCreateJmsTopic(config);
        System.out.println("Queue in relay is setup:" + blockingSetupJmsTopic);
        if (blockingSetupJmsTopic) {
            for (TopicId topic : config.getConfig().getTopicIds()) {
                log.info("Processing topic num: " + topic.getTopicNum());
                // create queue topic

                log.info("Queue is setup for topic num " + topic.getTopicNum());

                // subscribe to topic with mirror node
                MirrorTopicSubscriber subscriber = new MirrorTopicSubscriber(nodeAddress, nodePort, topic);
                Thread subscriberThread = new Thread(subscriber);
                subscriberThread.start();
            }
        } else {
            throw new Exception("Queue topic subscription error");
        }
    }

}
