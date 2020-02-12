package com.hedera.hcs.sxc.relay.subscribe;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

import com.google.common.util.concurrent.Uninterruptibles;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient.Subscription;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

/**
 *
 * Class to manage Mirror node topic subscribers
 * the subscription is a blocking gRPC process which requires its own thread
 * if multiple topics are to be subscribed to
 */
@Log4j2
public final class MirrorTopicSubscriber extends Thread {

    private String mirrorAddress = "";
    private int mirrorPort = 0;
    private ConsensusTopicId topicId;
    private boolean catchupHistory = false;
    private String consensusFile = "";
    private Optional<Instant> lastConsensusTimestamp = Optional.empty(); 
    
    public class SusbcriberCloseHook extends Thread {
        private Subscription subscription;
        public SusbcriberCloseHook(Subscription subscription) {
           this.subscription = subscription;
        }
        @Override
        public void run() {
            try {
                log.debug("SusbcriberCloseHook - closing");
                this.subscription.unsubscribe();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public MirrorTopicSubscriber(String mirrorAddress, int mirrorPort, ConsensusTopicId topicId, boolean catchupHistory, String consensusFile) {
        this.mirrorAddress = mirrorAddress;
        this.mirrorPort = mirrorPort;
        this.topicId = topicId;
        this.catchupHistory = catchupHistory;
        this.consensusFile = consensusFile;
    }

    public void run() {
        subscribe();
    }

    private void subscribe() {
        try (ConsensusClient subscriber = new ConsensusClient(this.mirrorAddress+ ":" + this.mirrorPort)
                .setErrorHandler(e -> {
                    log.debug("Attempting to reconnect");
                    subscribe();
                })
                
        )
        {
                try {
                    if (this.catchupHistory) {
                        // catchup history
                        this.lastConsensusTimestamp = Optional.of(Instant.EPOCH);
                        
                        File consensusTimeFile = new File(this.consensusFile);
                        if (consensusTimeFile.exists()) {
                            try(BufferedReader br = new BufferedReader(new FileReader(this.consensusFile))) {
                                String line = br.readLine();

                                String[] lastStoredConsensusTimestamp = line.split("-");
                                this.lastConsensusTimestamp = Optional.of(Instant.ofEpochSecond(Long.parseLong(lastStoredConsensusTimestamp[0]), Integer.parseInt(lastStoredConsensusTimestamp[1])));
                                this.lastConsensusTimestamp.get().plusNanos(1);
                            }
                        }
                    } else {
                        this.lastConsensusTimestamp = Optional.of(Instant.now());
                    }
                    
                    log.debug("Relay Subscribing to topic number " + this.topicId.toString() + " on mirror node: " + this.mirrorAddress + ":" + this.mirrorPort);

                    Subscription subscription = subscriber.subscribe(this.topicId, lastConsensusTimestamp.get(), tm -> {
                        log.debug("Got mirror message, calling handler");
                        lastConsensusTimestamp = Optional.of(tm.consensusTimestamp.plusNanos(1));
                        MirrorMessageHandler.onMirrorMessage(tm, this.topicId);
                    });
                    Runtime.getRuntime().addShutdownHook(new SusbcriberCloseHook(subscription));
                    for (;;) {
                        log.debug("Sleeping 30s");
                        Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(30));
                    }
                } catch (Exception e) {
                    log.error(e);
                    log.debug("Sleeping 10s before attempting connection again");
                    Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(10));
                }
        } catch (Exception e1) {
            log.error(e1);
            log.debug("Sleeping 11s before attempting connection again");
            Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(11));
        }

    }
}
