package com.hedera.hcs.sxc.queue.generator;

import com.hedera.hcs.sxc.queue.generator.config.Config;
import com.hedera.hcs.sxc.queue.generator.config.Queue;

public final class Generator {

    public static void main(String[] args) throws Exception {

        Queue queueConfig = new Config().getConfig().getQueue();
        switch (queueConfig.getProvider()) {
        case "google":
            GoogleGenerator.generate(queueConfig);
            break;
        case "mq":
            MqGenerator.generate(queueConfig);
            break;
        case "amazon":
            break;
        }
        System.out.println("Done");
    }
}