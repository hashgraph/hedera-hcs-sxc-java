package com.hedera.hcs.sxc.mq.generator;

import com.hedera.hcs.sxc.mq.generator.config.Config;
import com.hedera.hcs.sxc.mq.generator.config.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Log4j2
public final class Generator {

    public static void main(String[] args) throws InterruptedException, IOException {

        int iterations;

        while (true) {
            Queue queueConfig = new Config().getConfig().getQueue();
            iterations = queueConfig.getIterations();

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(queueConfig.getHost());
            factory.setPort(queueConfig.getPort());
            if ( ! queueConfig.getUser().isEmpty()) {
                factory.setUsername(queueConfig.getUser());
            }
            if ( ! queueConfig.getPassword().isEmpty()) {
                factory.setPassword(queueConfig.getPassword());
            }

            try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
                //channel.queueDeclare(queueConfig.getName(), false, false, false, null);
                channel.exchangeDeclare(queueConfig.getExchangeName(), "topic");

                do {
                    queueConfig = new Config().getConfig().getQueue();

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

                System.out.println("Done");
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception, sleeping 10s before retry");
                TimeUnit.SECONDS.sleep(10);
            }
        }
    }
}