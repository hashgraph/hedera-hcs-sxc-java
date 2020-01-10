package com.hedera.hcs.sxc.relay.subscribe;

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
    private int mirrorReconnectDelay = 0;
    private Instant nextRefreshTime = Instant.now();
    
    public class SusbcriberCloseHook extends Thread {
        private Subscription subscription;
        public SusbcriberCloseHook(Subscription subscription) {
           this.subscription = subscription;
        }
        @Override
        public void run() {
            try {
                log.info("SusbcriberCloseHook - closing");
                this.subscription.unsubscribe();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public MirrorTopicSubscriber(String mirrorAddress, int mirrorPort, ConsensusTopicId topicId, boolean catchupHistory, String consensusFile, int mirrorReconnectDelay) {
        this.mirrorAddress = mirrorAddress;
        this.mirrorPort = mirrorPort;
        this.topicId = topicId;
        this.catchupHistory = catchupHistory;
        this.consensusFile = consensusFile;
        this.mirrorReconnectDelay = mirrorReconnectDelay;
        if (this.mirrorReconnectDelay != 0) {
            // reconnect delay set to 0, wait indefinitely between reconnects
            this.nextRefreshTime = Instant.now().plus(Duration.ofDays(365));
        } else {
            this.nextRefreshTime = Instant.now().plusSeconds(this.mirrorReconnectDelay * 60);
        }
        
    }

    public void run() {
        subscribe();
    }

    private void subscribe() {
        try (ConsensusClient subscriber = new ConsensusClient(this.mirrorAddress+ ":" + this.mirrorPort)
                .setErrorHandler(e -> {
//                        log.error(e);
                    log.info("Attempting to reconnect");
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
                                StringBuilder sb = new StringBuilder();
                                String line = br.readLine();

                                String[] lastStoredConsensusTimestamp = line.split("-");
                                this.lastConsensusTimestamp = Optional.of(Instant.ofEpochSecond(Long.parseLong(lastStoredConsensusTimestamp[0]), Integer.parseInt(lastStoredConsensusTimestamp[1])));
                                this.lastConsensusTimestamp.get().plusNanos(1);
                            }
                        }
                    }
                    
                    log.info("Relay Subscribing to topic number " + this.topicId.toString() + " on mirror node: " + this.mirrorAddress + ":" + this.mirrorPort);

                    if (lastConsensusTimestamp.isPresent()) {
                        Subscription subscription = subscriber.subscribe(this.topicId, lastConsensusTimestamp.get(), tm -> {
                            log.info("Got mirror message, calling handler");
                            lastConsensusTimestamp = Optional.of(tm.consensusTimestamp.plusNanos(1));
                            MirrorMessageHandler.onMirrorMessage(tm, this.topicId);
                        });
                        completeSubscription(subscription);
                    } else {
                        Subscription subscription = subscriber.subscribe(this.topicId, tm -> {
                            log.info("Got mirror message, calling handler");
                            lastConsensusTimestamp = Optional.of(tm.consensusTimestamp.plusNanos(1));
                            MirrorMessageHandler.onMirrorMessage(tm, this.topicId);
                        });
                        completeSubscription(subscription);
                    }
                } catch (Exception e) {
                    log.error(e);
                    log.info("Sleeping 10s before attempting connection again");
                    Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(10));
                }
        } catch (Exception e1) {
            log.error(e1);
            log.info("Sleeping 11s before attempting connection again");
            Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(11));
        }

    }
    void completeSubscription(Subscription subscription) {
        log.info("Adding shutdown hook to subscription");
        Runtime.getRuntime().addShutdownHook(new SusbcriberCloseHook(subscription));
        for (;;) {
            log.info("Sleeping 30s");
            Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(30));
            if (Instant.now().compareTo(nextRefreshTime) > 0) {
                //  need to reconnect, no activity for this.mirrorReconnectDelay since last message
                nextRefreshTime = Instant.now().plusSeconds(this.mirrorReconnectDelay * 60);
                log.info("Unsusbscribing");
                subscription.unsubscribe();
                log.info("Unsusbscribed");
            }
        }
    }    
}
