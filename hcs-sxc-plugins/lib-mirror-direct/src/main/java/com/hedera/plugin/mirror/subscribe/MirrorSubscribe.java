package com.hedera.plugin.mirror.subscribe;

import java.time.Instant;
import java.util.Optional;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcslib.interfaces.HCSCallBackFromMirror;
import com.hedera.hcslib.interfaces.MirrorSubscriptionInterface;
import com.hedera.plugin.mirror.config.Config;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MirrorSubscribe implements MirrorSubscriptionInterface {

    @Override
    public void init(HCSCallBackFromMirror onHCSMessageCallback, long applicationId, Optional<Instant> lastConsensusTimestamp) throws Exception {
        log.info("lib-mirror-direct init");
        // subscribe
        
        Config config = new Config();

        String mirrorAddress = config.getConfig().getMirrorAddress();
        String[] mirrorDetails = mirrorAddress.split(":");
        if (mirrorDetails.length != 2) {
            throw new Exception("hcs-relay: mirrorAddress format is incorrect, should be address:port");
        }

        log.info("Subscribing to mirror node");
        for (ConsensusTopicId topic : config.getConfig().getTopicIds()) {
            log.info("Processing topic num: " + topic.topic);
            // subscribe to topic with mirror node
            MirrorTopicSubscriber subscriber = new MirrorTopicSubscriber(mirrorDetails[0], Integer.parseInt(mirrorDetails[1]), topic, lastConsensusTimestamp, onHCSMessageCallback);
            Thread subscriberThread = new Thread(subscriber);
            subscriberThread.start();
        }
    }
}

