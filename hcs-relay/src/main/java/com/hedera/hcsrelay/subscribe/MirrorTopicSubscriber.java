package com.hedera.hcsrelay.subscribe;

import java.util.concurrent.TimeUnit;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.consensus.TopicSubscriber;

import lombok.extern.log4j.Log4j2;

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
    private TopicId topicId;
    private TopicSubscriber subscriber;
    
    public class SusbcriberCloseHook extends Thread {
        private TopicSubscriber subscriber;
        public SusbcriberCloseHook(TopicSubscriber subscriber) {
           this.subscriber = subscriber;
        }
        @Override
        public void run() {
            try {
                log.info("SusbcriberCloseHook - closing");
                this.subscriber.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
    
    public MirrorTopicSubscriber(String mirrorAddress, int mirrorPort, TopicId topicId) {
        this.mirrorAddress = mirrorAddress;
        this.mirrorPort = mirrorPort;
        this.topicId = topicId;
    }
    
    public Runnable closeSubscription() {
        try {
            log.error("Closing subscription to mirror node");
            this.subscriber.close();
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public void run() {
        boolean retry = true;
        
        try (TopicSubscriber subscriber = new TopicSubscriber(this.mirrorAddress, this.mirrorPort))
        {
            log.info("Adding shutdown hook");
            Runtime.getRuntime().addShutdownHook(new SusbcriberCloseHook(subscriber));
            this.subscriber = subscriber;
        
            while (retry) {
                try {
                    log.info("Relay Subscribing to topic number " + this.topicId.getTopicNum() + " on mirror node: " + this.mirrorAddress + ":" + this.mirrorPort);
                    subscriber.subscribe(this.topicId, tm -> {
                        log.info("Got mirror message, calling handler");
                        MirrorMessageHandler.onMirrorMessage(tm, this.topicId);   
                    });
                    log.info("Relay Subscribed to topic number " + this.topicId.getTopicNum() + " on mirror node: " + this.mirrorAddress + ":" + this.mirrorPort);
                } catch (io.grpc.StatusRuntimeException e) {
                    log.info("Unable to connect to mirror node: " + mirrorAddress + ":" + mirrorPort + " topic: " + topicId.getTopicNum() + " - retrying in 3s");
                    log.error(e);
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    log.error(e);
                    log.error("****** EXITING RELAY SUBSRIBING THREAD");
                    retry = false;
                }
            }
        } catch (Exception e1) {
            log.error(e1);
        }
    }        
    
}