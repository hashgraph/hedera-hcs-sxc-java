package com.hedera.hcs.sxc.queue.google;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.SxcPersistence;
import com.hedera.hcs.sxc.queue.config.AppData;
import com.hedera.hcs.sxc.queue.config.Config;
import com.hedera.hcs.sxc.queue.config.Queue;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.threeten.bp.Duration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Log4j2
@Component
public class GooglePubSubListener {
    
    static class GoogleMessageReceiver implements MessageReceiver {

        @Override
        public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
            String received = message.getData().toStringUtf8();
            
            System.out.println(" [x] Received '" + message.getMessageId() + "':'" + received);
                try {
                    OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(AppData.getHCSCore());
                    outboundHCSMessage.sendMessage(0, received.getBytes());
                } catch (Exception ex) {
                    log.error(ex);
                }
            
            // Ack only after all work for the message is complete.
            consumer.ack();
        }
    }

    @PostConstruct
    public void init(){

        Queue queueConfig = null;
        try {
            queueConfig = new Config().getConfig().getQueue();
        } catch (IOException ex) {
            log.error(ex);
            return;
        }

        final Queue queueConfigFinal = queueConfig;
        
        if ( ! queueConfigFinal.getProvider().equals("google")) {
            return;
        }

        final String G_PROJECT_ID = queueConfigFinal.getExchangeName();
        final String G_PUB_TOPIC_ID = queueConfigFinal.getProducerTag();
        if (queueConfigFinal.getProducerTag() != null) {
            try {
                GoogleTopic.Create(G_PROJECT_ID, G_PUB_TOPIC_ID);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback;
        try {
            onHCSMessageCallback = new OnHCSMessageCallback(AppData.getHCSCore());
            onHCSMessageCallback.addObserver((SxcConsensusMessage sxcConsensusMesssage, HCSResponse hcsResponse) -> {
                // handle notification in mirrorNotification
                if (queueConfigFinal.getProducerTag() == null) {
                    System.out.println("got hcs response");
                    try {
                        System.out.println(" [x] Received '" + queueConfigFinal.getConsumerTag() + "':'"
                                + getSimpleDetails(AppData.getHCSCore(), hcsResponse) + "'");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("got hcs response - feeding back to out queue");
                    ProjectTopicName pubTopicName = ProjectTopicName.of(G_PROJECT_ID, G_PUB_TOPIC_ID);
                    Publisher publisher;
                    try {
                        BatchingSettings batchingSettings = BatchingSettings.newBuilder()
                                .setDelayThreshold(Duration.ofSeconds(1)).build();
                        publisher = Publisher.newBuilder(pubTopicName).setBatchingSettings(batchingSettings).build();
                        ByteString data = ByteString.copyFromUtf8(hcsResponse.toString());
                        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
                        publisher.publish(pubsubMessage);
                        publisher.shutdown();
                        System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'"
                                + getSimpleDetails(AppData.getHCSCore(), hcsResponse) + "'");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        try {
            System.out.println("Loaded APP_ID:" + AppData.getHCSCore().getApplicationId());
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Runnable runnable;
        runnable = () -> {

            final String G_SUB_TOPIC_ID = queueConfigFinal.getConsumerTag();

            try {
                GoogleTopic.Create(G_PROJECT_ID, G_SUB_TOPIC_ID);
                
                ProjectTopicName subTopicName = ProjectTopicName.of(G_PROJECT_ID, G_SUB_TOPIC_ID);
                ProjectSubscriptionName subscriptionName =
                        ProjectSubscriptionName.of(G_PROJECT_ID, "subscription-" + G_SUB_TOPIC_ID);

                SubscriptionAdminClient subscriptionAdminClient = null;
                try {
                    subscriptionAdminClient = SubscriptionAdminClient.create();
                    subscriptionAdminClient.createSubscription(subscriptionName, subTopicName, PushConfig.newBuilder().build(), 600);
                } catch (AlreadyExistsException e) {
                    System.out.println("Subscription already exists.");
                }
                // create a subscriber bound to the asynchronous message receiver
                Subscriber subscriber = Subscriber.newBuilder(subscriptionName, new GoogleMessageReceiver()).build();
                subscriber.startAsync().awaitRunning();
                // Allow the subscriber to run indefinitely unless an unrecoverable error occurs.
                subscriber.awaitTerminated();
            } catch (IllegalStateException e) {
                System.out.println("Subscriber unexpectedly stopped: " + e);
            } catch (IOException e) {
                System.out.println("Error occurred: " + e);
                return;
            }                
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private static String getSimpleDetails(HCSCore hcsCore, HCSResponse hcsResponse) {
        String ret = null;
        try {
            SxcApplicationMessageInterface applicationMessageEntity = hcsCore.getPersistence()
                    .getApplicationMessageEntity(
                            SxcPersistence.extractApplicationMessageStringId(hcsResponse.getApplicationMessageId()));
            ret =   hcsCore.getTopics().get(0).getTopic() + "|"
                    + applicationMessageEntity.getLastChronoPartSequenceNum() + "|"
                    + applicationMessageEntity.getLastChronoPartConsensusTimestamp() + "|"
                    + ApplicationMessage.parseFrom(applicationMessageEntity.getApplicationMessage())
                            .getBusinessProcessMessage().toString("UTF-8");
            ;

        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        } catch (InvalidProtocolBufferException ex) {
            log.error(ex);
        }
        return ret;
    }
}
