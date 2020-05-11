package com.hedera.hcs.sxc.queue.amazon;

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
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class AmazonSQSListener {
    
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
        
        if ( ! queueConfigFinal.getProvider().equals("amazon")) {
            return;
        }

        final String QUEUE_NAME = queueConfig.getProducerTag();
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        if (queueConfigFinal.getProducerTag() != null) {
            try {
                sqs.createQueue(QUEUE_NAME);
            } catch (AmazonSQSException e) {
                if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                    throw e;
                }
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
                    String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

                    SendMessageRequest send_msg_request = new SendMessageRequest()
                            .withQueueUrl(queueUrl)
                            .withMessageBody(hcsResponse.toString())
                            .withDelaySeconds(0);
                    sqs.sendMessage(send_msg_request);

                    try {
                        System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'"
                                    + getSimpleDetails(AppData.getHCSCore(), hcsResponse) + "'");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
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

            final String QUEUE = queueConfigFinal.getConsumerTag();

            try {
                try {
                    sqs.createQueue(QUEUE);
                } catch (AmazonSQSException e) {
                    if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                        throw e;
                    }
                }
                String queueUrl = sqs.getQueueUrl(QUEUE).getQueueUrl();
                while (true) {
                 // receive messages from the queue
                    List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();

                    // delete messages from the queue
                    for (Message message : messages) {
                        String received = message.getBody();
                        System.out.println(" [x] Received '" + message.getMessageId() + "':'" + received);
                        try {
                            OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(AppData.getHCSCore());
                            outboundHCSMessage.sendMessage(0, received.getBytes());
                        } catch (Exception ex) {
                            log.error(ex);
                        }
                        sqs.deleteMessage(queueUrl, message.getReceiptHandle());
                    }
                    // sleep a few seconds
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                
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
