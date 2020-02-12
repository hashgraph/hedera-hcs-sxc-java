package com.hedera.hcs.sxc.plugin.mirror.subscribe;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

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
    public void init(HCSCallBackFromMirror onHCSMessageCallback, long applicationId, Optional<Instant> lastConsensusTimestamp, String mirrorAddress, List<ConsensusTopicId> topicIds) throws Exception {
        log.debug("hcs-sxc-java-plugins-mirror-direct init");
        // subscribe
        
        String[] mirrorDetails = mirrorAddress.split(":");
        if (mirrorDetails.length != 2) {
            throw new Exception("mirrorAddress format is incorrect, should be address:port");
        }

        log.debug("Subscribing to mirror node");
        for (ConsensusTopicId topic : topicIds) {
            log.debug("Processing topic num: " + topic.toString());
            // subscribe to topic with mirror node
            MirrorTopicSubscriber subscriber = new MirrorTopicSubscriber(mirrorDetails[0], Integer.parseInt(mirrorDetails[1]), topic, lastConsensusTimestamp, onHCSMessageCallback);
            Thread subscriberThread = new Thread(subscriber);
            subscriberThread.start();
        }
    }
}

