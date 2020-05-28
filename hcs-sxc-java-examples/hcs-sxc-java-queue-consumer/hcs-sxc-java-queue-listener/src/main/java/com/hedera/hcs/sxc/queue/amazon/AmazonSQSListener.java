package com.hedera.hcs.sxc.queue.amazon;

import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.queue.HCSMessageRest;
import com.hedera.hcs.sxc.queue.Utils;
import com.hedera.hcs.sxc.queue.config.Config;
import com.hedera.hcs.sxc.queue.config.Queue;
import com.hedera.hcs.sxc.queue.config.Sqs;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@RestController
public class AmazonSQSListener {
    
    private HCSCore hcsCore;
    
    @PostConstruct
    public void init() throws Exception{

        Queue queueConfig = null;
        try {
            queueConfig = new Config().getConfig().getQueue();
        } catch (IOException ex) {
            log.error(ex);
            return;
        }

        final Sqs queueConfigFinal = queueConfig.getSqs();
        
        if ( ! queueConfigFinal.getEnabled()) {
            return;
        }

        this.hcsCore = new HCSCore()
                .withTopic(queueConfigFinal.getTopicId())
                .builder("pubsub",
                        "./config/config.yaml",
                        "./config/.env"
                );
        
        final String QUEUE_NAME = queueConfig.getSqs().getProducerTag();
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
                    String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
                    String response = Utils.JSONPublishMessage(sxcConsensusMesssage, hcsResponse);
                    
                    SendMessageRequest send_msg_request = new SendMessageRequest()
                            .withQueueUrl(queueUrl)
                            .withMessageBody(response.toString())
                            .withDelaySeconds(0);
                    sqs.sendMessage(send_msg_request);

                    try {
                        System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'"
                                    + Utils.getSimpleDetails(this.hcsCore, hcsResponse) + "'");
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
            System.out.println("Loaded APP_ID:" + this.hcsCore.getApplicationId());
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
                            OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(this.hcsCore);
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

    @GetMapping(value = "/sqs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HCSMessageRest> hcsMessages() throws Exception {
        return Utils.restResponse(this.hcsCore);
    }
}
