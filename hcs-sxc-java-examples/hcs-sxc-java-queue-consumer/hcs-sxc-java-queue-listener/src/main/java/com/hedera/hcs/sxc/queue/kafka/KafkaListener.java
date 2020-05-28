package com.hedera.hcs.sxc.queue.kafka;

import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.queue.HCSMessageRest;
import com.hedera.hcs.sxc.queue.Utils;
import com.hedera.hcs.sxc.queue.config.Config;
import com.hedera.hcs.sxc.queue.config.Kafka;
import com.hedera.hcs.sxc.queue.config.Queue;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Arrays;

@Log4j2
@RestController
public class KafkaListener {
    
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

        final Kafka queueConfigFinal = queueConfig.getKafka();

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

                Properties props = new Properties();
                String server = queueConfigFinal.getHost() + ":" + queueConfigFinal.getPort();
                props.put("bootstrap.servers", server);
                props.put("acks", "all");
                props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

                Producer<String, String> producer = new KafkaProducer<>(props);
                String message = Utils.JSONPublishMessage(sxcConsensusMesssage, hcsResponse);
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(queueConfigFinal.getProducerTag(), message);
                producer.send(record);

                System.out.println(" [x] Sent '" + queueConfigFinal.getProducerTag() + "':'"
                            + Utils.getSimpleDetails(this.hcsCore, hcsResponse) + "'");
            }
        });

        System.out.println("Loaded APP_ID:" + this.hcsCore.getApplicationId());

        Runnable runnable;
        runnable = () -> {
            
            Properties props = new Properties();
            String server = queueConfigFinal.getHost() + ":" + queueConfigFinal.getPort();
            props.setProperty("bootstrap.servers", server);
            props.setProperty("group.id", "test");
            props.setProperty("enable.auto.commit", "true");
            props.setProperty("auto.commit.interval.ms", "1000");
            props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(queueConfigFinal.getConsumerTag()));
            System.out.println("Kafka listening");
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value(); //new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" + message
                            + "'" + "on " + queueConfigFinal.getConsumerTag());
                    try {
                        OutboundHCSMessage outboundHCSMessage = new OutboundHCSMessage(this.hcsCore);
                        outboundHCSMessage.sendMessage(0, message.getBytes());
                    } catch (Exception ex) {
                        log.error(ex);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @GetMapping(value = "/kafka", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HCSMessageRest> hcsMessages() throws Exception {
        return Utils.restResponse(this.hcsCore);
    }
}
