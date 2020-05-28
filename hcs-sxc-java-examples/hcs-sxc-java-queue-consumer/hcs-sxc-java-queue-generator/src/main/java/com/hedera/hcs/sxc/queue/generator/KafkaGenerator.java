package com.hedera.hcs.sxc.queue.generator;

import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.hedera.hcs.sxc.queue.config.Queue;

public class KafkaGenerator {
    public static void generate(Queue queueConfig) {
        int iterations = queueConfig.getIterations();

        if ( ! queueConfig.getKafka().getEnabled()) {
            return;
        }

        Properties props = new Properties();
        String server = queueConfig.getKafka().getHost() + ":" + queueConfig.getKafka().getPort();
        props.put("bootstrap.servers", server);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        do {
            try {
            String message = Data.getRandomData();
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(queueConfig.getKafka().getConsumerTag(), message);
            producer.send(record);
            
            System.out.println(" Sent '" + message + "'");

            iterations = iterations - 1;
            if (iterations < 0) {
                iterations = 0;
            }

            TimeUnit.MILLISECONDS.sleep(queueConfig.getDelayMillis());

            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while ((iterations > 0) || (queueConfig.getIterations() == 0));

        producer.close();
        
    }
}
