package com.hedera.hcs.sxc.queue.google;

import com.google.protobuf.ByteString;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.queue.HCSMessageRest;
import com.hedera.hcs.sxc.queue.Utils;
import com.hedera.hcs.sxc.queue.config.Config;
import com.hedera.hcs.sxc.queue.config.Pubsub;
import com.hedera.hcs.sxc.queue.config.Queue;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.Duration;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Log4j2
@RestController
public class GooglePubSubListener {
    private HCSCore hcsCore;
    
    static class GoogleMessageReceiver implements MessageReceiver {

        HCSCore hcsCore;
        
        public GoogleMessageReceiver(HCSCore hcsCore) {
            this.hcsCore = hcsCore;
        }
        
        @Override
        public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
            String received = message.getData().toStringUtf8();
            
            System.out.println(" [x] Received '" + message.getMessageId() + "':'" + received);
                try {
                    OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(this.hcsCore);
                    outboundHCSMessage.sendMessage(0, received.getBytes());
                } catch (Exception ex) {
                    log.error(ex);
                }
            
            // Ack only after all work for the message is complete.
            consumer.ack();
        }
    }

    @PostConstruct
    public void init() throws Exception{

        Queue queueConfig = null;
        try {
            queueConfig = new Config().getConfig().getQueue();
        } catch (IOException ex) {
            log.error(ex);
            return;
        }

        final Pubsub queueConfigFinal = queueConfig.getPubSub();
        
        if ( ! queueConfigFinal.getEnabled()) {
            return;
        }

        this.hcsCore = new HCSCore()
                .withTopic(queueConfigFinal.getTopicId())
                .builder("pubsub",
                        "./config/config.yaml",
                        "./config/.env"
                );

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
            onHCSMessageCallback = new OnHCSMessageCallback(this.hcsCore);
            onHCSMessageCallback.addObserver((SxcConsensusMessage sxcConsensusMesssage, HCSResponse hcsResponse) -> {
                // handle notification in mirrorNotification
                if (queueConfigFinal.getProducerTag() == null) {
                    System.out.println("got hcs response");
                    try {
                        System.out.println(" [x] Received '" + queueConfigFinal.getConsumerTag() + "':'"
                                + Utils.getSimpleDetails(this.hcsCore, hcsResponse) + "'");
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
                        
                        String response = Utils.JSONPublishMessage(sxcConsensusMesssage, hcsResponse);
                        
                        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(response.toString())).build();
                        publisher.publish(pubsubMessage);
                        publisher.shutdown();
                        System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'"
                                + Utils.getSimpleDetails(this.hcsCore, hcsResponse) + "'");
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
            System.out.println("Loaded APP_ID:" + this.hcsCore.getApplicationId());
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
                Subscriber subscriber = Subscriber.newBuilder(subscriptionName, new GoogleMessageReceiver(this.hcsCore)).build();
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

    @GetMapping(value = "/pubsub", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HCSMessageRest> hcsMessages() throws Exception {
        return Utils.restResponse(this.hcsCore);
    }
}
