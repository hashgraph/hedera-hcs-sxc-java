package com.hedera.hcs.sxc.plugin.mirror.subscribe;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessage;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient.Subscription;
import com.hedera.hcs.sxc.interfaces.HCSCallBackFromMirror;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.proto.java.ApplicationMessageChunk;

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
    private HCSCallBackFromMirror onHCSMessageCallback;
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
    
    public MirrorTopicSubscriber(String mirrorAddress, int mirrorPort, ConsensusTopicId topicId, Optional<Instant> subscribeFrom, HCSCallBackFromMirror onHCSMessageCallback, int mirrorReconnectDelay) {
        this.mirrorAddress = mirrorAddress;
        this.mirrorPort = mirrorPort;
        this.topicId = topicId;
        this.subscribeFrom = subscribeFrom;
        this.onHCSMessageCallback = onHCSMessageCallback;
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
                
                log.info("App Subscribing to topic number " + this.topicId.toString() + " on mirror node: " + this.mirrorAddress + ":" + this.mirrorPort);

                if (this.subscribeFrom.isPresent()) {
                    this.subscribeFrom = Optional.of(this.subscribeFrom.get().plusNanos(1));
                    Subscription subscription = subscriber.subscribe(this.topicId, this.subscribeFrom.get(), tm -> {
                        log.info("Got mirror message, calling handler");
                        this.subscribeFrom = Optional.of(tm.consensusTimestamp.plusNanos(1));
                        onMirrorMessage(tm, this.onHCSMessageCallback);   
                    });
                    completeSubscription(subscription);
                } else {
                    Subscription subscription = subscriber.subscribe(this.topicId, tm -> {
                        log.info("Got mirror message, calling handler");
                        this.subscribeFrom = Optional.of(tm.consensusTimestamp.plusNanos(1));
                        onMirrorMessage(tm, this.onHCSMessageCallback);   
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
    
    void onMirrorMessage(ConsensusMessage messagesResponse, HCSCallBackFromMirror onHCSMessageCallback) {
          log.info("Got message from mirror - persisting");
    
          onHCSMessageCallback.storeMirrorResponse(messagesResponse);
          
          byte[] message = messagesResponse.message;
          ApplicationMessageChunk messagePart;
        try {
            messagePart = ApplicationMessageChunk.parseFrom(message);
            log.info("Got message from mirror - calling back");
            onHCSMessageCallback.partialMessage(messagePart);
        } catch (InvalidProtocolBufferException e) {
            log.error(e);
        }

        log.info("Got message from mirror - acknowledged");
      
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