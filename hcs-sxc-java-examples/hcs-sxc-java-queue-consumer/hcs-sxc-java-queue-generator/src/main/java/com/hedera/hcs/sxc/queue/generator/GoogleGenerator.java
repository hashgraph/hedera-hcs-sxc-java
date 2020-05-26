package com.hedera.hcs.sxc.queue.generator;

import java.util.concurrent.TimeUnit;

import org.threeten.bp.Duration;

import com.google.api.gax.batching.BatchingSettings;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.hedera.hcs.sxc.queue.config.Queue;

public class GoogleGenerator {
    public static void generate(Queue queueConfig) throws Exception {
        int iterations = queueConfig.getIterations();

        if ( ! queueConfig.getPubSub().getEnabled()) {
            return;
        }

        final String G_PROJECT_ID = queueConfig.getPubSub().getExchangeName();
        final String G_TOPIC_ID = queueConfig.getPubSub().getConsumerTag();
        GoogleTopic.Create(G_PROJECT_ID, G_TOPIC_ID);

        ProjectTopicName pubTopicName = ProjectTopicName.of(G_PROJECT_ID, G_TOPIC_ID);
        Publisher publisher;

        do {
            String message = Data.getRandomData();

            BatchingSettings batchingSettings = BatchingSettings.newBuilder()
                    .setDelayThreshold(Duration.ofSeconds(1)).build();
            publisher = Publisher.newBuilder(pubTopicName).setBatchingSettings(batchingSettings).build();
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            publisher.publish(pubsubMessage);
            publisher.shutdown();
            System.out.println(" Sent '" + message + "'");

            iterations = iterations - 1;
            if (iterations < 0) {
                iterations = 0;
            }

            TimeUnit.MILLISECONDS.sleep(queueConfig.getDelayMillis());
        } while ((iterations > 0) || (queueConfig.getIterations() == 0));
    }
}
