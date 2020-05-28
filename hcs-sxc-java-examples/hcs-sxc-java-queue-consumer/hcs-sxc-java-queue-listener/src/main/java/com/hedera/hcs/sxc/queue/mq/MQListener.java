package com.hedera.hcs.sxc.queue.mq;

import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.queue.HCSMessageRest;
import com.hedera.hcs.sxc.queue.Utils;
import com.hedera.hcs.sxc.queue.config.Config;
import com.hedera.hcs.sxc.queue.config.Mq;
import com.hedera.hcs.sxc.queue.config.Queue;
import com.rabbitmq.client.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Log4j2
@RestController
public class MQListener {
    
    private HCSCore hcsCore;

    @PostConstruct
    public void init() throws Exception {

        Queue queueConfig = null;
        try {
            queueConfig = new Config().getConfig().getQueue();
        } catch (IOException ex) {
            log.error(ex);
            return;
        }

        final Mq queueConfigFinal = queueConfig.getMq();

        if ( ! queueConfigFinal.getEnabled()) {
            return;
        }

        this.hcsCore = new HCSCore()
                .withTopic(queueConfigFinal.getTopicId())
                .builder("pubsub",
                        "./config/config.yaml",
                        "./config/.env"
                );
        
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(this.hcsCore);
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

                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(queueConfigFinal.getHost());
                factory.setUsername(queueConfigFinal.getUser());
                factory.setPassword(queueConfigFinal.getPassword());
                factory.setPort(queueConfigFinal.getPort());

                try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
                    channel.exchangeDeclare(queueConfigFinal.getExchangeName(), "topic");

                    String response = Utils.JSONPublishMessage(sxcConsensusMesssage, hcsResponse);

                    channel.basicPublish(queueConfigFinal.getExchangeName(), queueConfigFinal.getProducerTag(), null,
                            response.getBytes());
                    System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'"
                            + Utils.getSimpleDetails(this.hcsCore, hcsResponse) + "'");
                } catch (IOException ex) {
                    log.error(ex);
                } catch (TimeoutException ex) {
                    log.error(ex);
                } catch (Exception ex) {
                    log.error(ex);
                }
            }
        });

        System.out.println("Loaded APP_ID:" + this.hcsCore.getApplicationId());

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
                        OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(this.hcsCore);
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

    @GetMapping(value = "/mq", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HCSMessageRest> hcsMessages() throws Exception {
        return Utils.restResponse(this.hcsCore);
    }
}
