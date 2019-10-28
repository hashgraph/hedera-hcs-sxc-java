package com.hedera.hcsrelay.subscribe;

import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.consensus.TopicSubscriber;
import com.hedera.mirror.api.proto.java.MirrorGetTopicMessages.MirrorGetTopicMessagesResponse;

/**
 * 
 * Class to manage Mirror node topic subscribers
 * the subscription is a blocking gRPC process which requires its own thread
 * if multiple topics are to be subscribed to
 */
public final class MirrorTopicSubscriber extends Thread {
    
    private String mirrorAddress = "";
    private int mirrorPort = 0;
    private TopicId topicId;
    private TopicSubscriber subscriber;
    
    public MirrorTopicSubscriber(String mirrorAddress, int mirrorPort, TopicId topicId) {
        this.mirrorAddress = mirrorAddress;
        this.mirrorPort = mirrorPort;
        this.topicId = topicId;
    }
    
    public void onMirrorMessage(MirrorGetTopicMessagesResponse consumer) {
        System.out.println("Got message from mirror node: " + consumer.getConsensusTimestamp() + consumer.getMessage().toString());
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
        try (TopicSubscriber subscriber = new TopicSubscriber(this.mirrorAddress, this.mirrorPort))
        {
            Runtime.getRuntime().addShutdownHook(new Thread(closeSubscription()));
            this.subscriber = subscriber;
            subscriber.subscribe(this.topicId, consumer -> onMirrorMessage(consumer));
        } catch (Exception e) {
            e.printStackTrace();
        };
    }        
    
}