package com.hedera.hcs.sxc.queue.generator;

import java.util.concurrent.TimeUnit;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hedera.hcs.sxc.queue.generator.config.Queue;

public class AmazonGenerator {
    public static void generate(Queue queueConfig) throws Exception {
        
        int iterations = queueConfig.getIterations();
        final String QUEUE_NAME = queueConfig.getConsumerTag();
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        try {
            sqs.createQueue(QUEUE_NAME);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw e;
            }
        }
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

        do {
            String message = Data.getRandomData();

            SendMessageRequest send_msg_request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(message)
                    .withDelaySeconds(0);
            sqs.sendMessage(send_msg_request);
            
            System.out.println(" Sent '" + message + "'");

            iterations = iterations - 1;
            if (iterations < 0) {
                iterations = 0;
            }

            TimeUnit.MILLISECONDS.sleep(queueConfig.getDelayMillis());
        } while ((iterations > 0) || (queueConfig.getIterations() == 0));
    }
}
