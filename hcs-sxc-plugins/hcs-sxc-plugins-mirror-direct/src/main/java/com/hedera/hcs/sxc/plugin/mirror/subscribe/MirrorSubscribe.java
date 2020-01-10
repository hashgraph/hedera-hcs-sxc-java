package com.hedera.hcs.sxc.plugin.mirror.subscribe;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hcs.sxc.interfaces.MirrorSubscriptionInterface;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MirrorSubscribe implements MirrorSubscriptionInterface {

    @Override
    public void init(HCSCallBackFromMirror onHCSMessageCallback, long applicationId, Optional<Instant> lastConsensusTimestamp, String mirrorAddress, List<ConsensusTopicId> topicIds, int mirrorReconnectDelay) throws Exception {
        log.info("hcs-sxc-plugins-mirror-direct init");
        // subscribe
        
        String[] mirrorDetails = mirrorAddress.split(":");
        if (mirrorDetails.length != 2) {
            throw new Exception("mirrorAddress format is incorrect, should be address:port");
        }

        log.info("Subscribing to mirror node");
        for (ConsensusTopicId topic : topicIds) {
            log.info("Processing topic num: " + topic.toString());
            // subscribe to topic with mirror node
            MirrorTopicSubscriber subscriber = new MirrorTopicSubscriber(mirrorDetails[0], Integer.parseInt(mirrorDetails[1]), topic, lastConsensusTimestamp, onHCSMessageCallback, mirrorReconnectDelay);
            Thread subscriberThread = new Thread(subscriber);
            subscriberThread.start();
        }
    }
}

