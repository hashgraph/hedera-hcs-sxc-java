package com.hedera.hcs.sxc.queue.generator;

import java.util.concurrent.TimeUnit;

import com.hedera.hcs.sxc.queue.generator.config.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MqGenerator {
    public static void generate(Queue queueConfig) {
        int iterations = queueConfig.getIterations();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueConfig.getHost());
        if ( ! queueConfig.getUser().isEmpty()) {
            factory.setUsername(queueConfig.getUser());
        }
        if ( ! queueConfig.getPassword().isEmpty()) {
            factory.setPassword(queueConfig.getPassword());
        }

        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(queueConfig.getExchangeName(), "topic");

            do {
                String message = Data.getRandomData();
                //channel.basicPublish("", queueConfig.getName(), null, message.getBytes());
                channel.basicPublish(queueConfig.getExchangeName(), queueConfig.getConsumerTag(), null, message.getBytes());
               
                System.out.println(" Sent '" + message + "'");

                iterations = iterations - 1;
                if (iterations < 0) {
                    iterations = 0;
                }

                TimeUnit.MILLISECONDS.sleep(queueConfig.getDelayMillis());
            } while ((iterations > 0) || (queueConfig.getIterations() == 0));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception, sleeping 10s before retry");
        }
    }
}
