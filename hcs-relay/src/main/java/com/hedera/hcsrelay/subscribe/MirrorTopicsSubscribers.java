package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hcsrelay.config.Config;

/**
 * Subscribes to topic(s) against a mirror node
 *
 */
public final class MirrorTopicsSubscribers {

    public MirrorTopicsSubscribers() throws Exception {
        Config config = new Config();
        
        String mirrorAddress = config.getConfig().getMirrorAddress();
        String[] mirrorDetails = mirrorAddress.split(":");
        if (mirrorDetails.length != 2) {
            throw new Exception("hcs-relay: mirrorAddress format is incorrect, should be address:port");
        }
                
        for (TopicId topic : config.getConfig().getTopicIds()) {
            System.out.println("Subscribing to topic number " + topic.getTopicNum());
            // subscribe to topic with mirror node
            MirrorTopicSubscriber subscriber = new MirrorTopicSubscriber(mirrorDetails[0], Integer.parseInt(mirrorDetails[1]), topic);
            new Thread(subscriber);
        }
    }
    
}
