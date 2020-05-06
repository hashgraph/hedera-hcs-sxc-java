package com.hedera.hcs.sxc.queue.mq;

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
import com.rabbitmq.client.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

@Log4j2
@Component
public class Listener {
    @PostConstruct
    public void init() throws Exception {

        Queue queueConfig = null;
        try {
            queueConfig = new Config().getConfig().getQueue();
        } catch (IOException ex) {
            log.error(ex);
            return;
        }

        final Queue queueConfigFinal = queueConfig;

        if ( ! queueConfigFinal.getProvider().equals("mq")) {
            return;
        }

        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(AppData.getHCSCore());
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

                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(queueConfigFinal.getHost());
                factory.setUsername(queueConfigFinal.getUser());
                factory.setPassword(queueConfigFinal.getPassword());
                factory.setPort(queueConfigFinal.getPort());

                try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
                    channel.exchangeDeclare(queueConfigFinal.getExchangeName(), "topic");
                    channel.basicPublish(queueConfigFinal.getExchangeName(), queueConfigFinal.getProducerTag(), null,
                            getSimpleDetails(AppData.getHCSCore(), hcsResponse).getBytes());
                    System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'"
                            + getSimpleDetails(AppData.getHCSCore(), hcsResponse) + "'");
                } catch (IOException ex) {
                    log.error(ex);
                } catch (TimeoutException ex) {
                    log.error(ex);
                } catch (Exception ex) {
                    log.error(ex);
                }
            }
        });

        System.out.println("Loaded APP_ID:" + AppData.getHCSCore().getApplicationId());

        Runnable runnable;
        runnable = () -> {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(queueConfigFinal.getHost());
            factory.setUsername(queueConfigFinal.getUser());
            factory.setPassword(queueConfigFinal.getPassword());
            factory.setPort(queueConfigFinal.getPort());

            try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
                channel.exchangeDeclare(queueConfigFinal.getExchangeName(), "topic");
                String consumerTag = queueConfigFinal.getConsumerTag();
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, queueConfigFinal.getExchangeName(), consumerTag);
                System.out.println("Connected");

                DeliverCallback deliverCallback = (consumerTagPrime, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message
                            + "'" + "on " + consumerTagPrime);
                    try {
                        OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(AppData.getHCSCore());
                        outboundHCSMessage.sendMessage(0, message.getBytes());
                    } catch (Exception ex) {
                        log.error(ex);
                    }
                };
                channel.basicConsume(queueName, true, deliverCallback, consumerTagPrime -> {
                    System.out.println("woot");
                });

                Object lock = new Object();
                synchronized (lock) {
                    lock.wait();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
