package com.hedera.hcs.sxc.queue.generator;

import java.util.concurrent.TimeUnit;

import com.hedera.hcs.sxc.queue.config.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MqGenerator {
    public static void generate(Queue queueConfig) {
        int iterations = queueConfig.getIterations();

        if ( ! queueConfig.getMq().getEnabled()) {
            return;
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueConfig.getMq().getHost());
        if ( ! queueConfig.getMq().getUser().isEmpty()) {
            factory.setUsername(queueConfig.getMq().getUser());
        }
        if ( ! queueConfig.getMq().getPassword().isEmpty()) {
            factory.setPassword(queueConfig.getMq().getPassword());
        }

        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(queueConfig.getMq().getExchangeName(), "topic");

            do {
                String message = Data.getRandomData();
                //channel.basicPublish("", queueConfig.getName(), null, message.getBytes());
                channel.basicPublish(queueConfig.getMq().getExchangeName(), queueConfig.getMq().getConsumerTag(), null, message.getBytes());
               
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
