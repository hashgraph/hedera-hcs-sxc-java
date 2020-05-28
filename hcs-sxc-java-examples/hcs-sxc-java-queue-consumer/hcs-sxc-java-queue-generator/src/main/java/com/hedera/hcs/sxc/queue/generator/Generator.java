package com.hedera.hcs.sxc.queue.generator;

import com.hedera.hcs.sxc.queue.config.Config;
import com.hedera.hcs.sxc.queue.config.Queue;

public final class Generator {

    public static void main(String[] args) throws Exception {

        Queue queueConfig = new Config().getConfig().getQueue();
        
        GoogleGenerator.generate(queueConfig);
        MqGenerator.generate(queueConfig);
        AmazonGenerator.generate(queueConfig);
        KafkaGenerator.generate(queueConfig);
        System.out.println("Done");
    }
}