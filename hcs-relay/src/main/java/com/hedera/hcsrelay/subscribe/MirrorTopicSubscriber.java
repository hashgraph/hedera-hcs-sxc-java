package com.hedera.hcsrelay.subscribe;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    private Optional<Instant> subscribeFrom;
    
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
    
    public MirrorTopicSubscriber(String mirrorAddress, int mirrorPort, ConsensusTopicId topicId, Optional<Instant> subscribeFrom) {
        this.mirrorAddress = mirrorAddress;
        this.mirrorPort = mirrorPort;
        this.topicId = topicId;
        this.subscribeFrom = subscribeFrom;
    }
    
    public void run() {
        subscribe();
    }
    
    private void subscribe() {
        boolean retry = true;

        try (ConsensusClient subscriber = new ConsensusClient(this.mirrorAddress+ ":" + this.mirrorPort)
                .setErrorHandler(e -> {
                    log.error(e);
                    log.info("Sleeping 3s before attempting connection again");
                    Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(3));
                    subscribe();
                })
                
        )
        {
//            this.subscriber = subscriber;
            if (subscriber != null) {
                while (retry) {
                    try {
                        
                        log.info("Relay Subscribing to topic number " + this.topicId.toString() + " on mirror node: " + this.mirrorAddress + ":" + this.mirrorPort);
    
                        Subscription subscription;
                        if (this.subscribeFrom.isPresent()) {
                            subscription = subscriber.subscribe(this.topicId, this.subscribeFrom.get(), tm -> {
                                log.info("Got mirror message, calling handler");
                                MirrorMessageHandler.onMirrorMessage(tm, this.topicId);   
                            });
                            
                        } else {
                            subscription = subscriber.subscribe(this.topicId, tm -> {
                                log.info("Got mirror message, calling handler");
                                MirrorMessageHandler.onMirrorMessage(tm, this.topicId);   
                            });
                        }
                        
                        log.info("Adding shutdown hook to subscription");
                        Runtime.getRuntime().addShutdownHook(new SusbcriberCloseHook(subscription));
    
                        for (; ;) {
                            Thread.sleep(2500);
                        }
                    } catch (Exception e) {
                        log.error(e);
                        retry = false;
                    }
                }
            }    
        } catch (Exception e1) {
            log.error(e1);
        }
        
    }    
}