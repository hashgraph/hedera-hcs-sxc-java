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
                this.subscriber.close();
            } catch (Exception e) {
                e.printStackTrace();
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
            this.subscriber.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run() {
        boolean retry = true;
        
        try (TopicSubscriber subscriber = new TopicSubscriber(this.mirrorAddress, this.mirrorPort))
        {
            Runtime.getRuntime().addShutdownHook(new SusbcriberCloseHook(subscriber));
            this.subscriber = subscriber;
        
            while (retry) {
                try {
                    log.info("Mirror Subscribing to topic number " + this.topicId.getTopicNum() + " on mirror node: " + this.mirrorAddress + ":" + this.mirrorPort);
                    subscriber.subscribe(this.topicId, (tm) -> {
                        MirrorMessageHandler.onMirrorMessage(tm, this.topicId);   
                    });
                } catch (io.grpc.StatusRuntimeException e) {
                    log.info("Unable to connect to mirror node: " + mirrorAddress + ":" + mirrorPort + " topic: " + topicId.getTopicNum() + " - retrying in 3s");
                    e.printStackTrace();
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    e.printStackTrace();
                    retry = false;
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }        
    
}