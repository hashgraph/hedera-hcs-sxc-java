package com.hedera.hcs.sxc.queue.google;

import java.io.IOException;

import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode.Code;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectTopicName;

public class GoogleTopic {
    public static void Create(String projectId, String topicId) throws IOException {
        // create the topic
        ProjectTopicName topic = ProjectTopicName.of(projectId, topicId);
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
          topicAdminClient.createTopic(topic);
          System.out.printf("Topic %s:%s created.\n", topic.getProject(), topic.getTopic());
        } catch (ApiException e) {
          // example : code = ALREADY_EXISTS(409) implies topic already exists
            if (e.getStatusCode().getCode() == Code.ALREADY_EXISTS) {
                System.out.println("Topic " + topicId + " in project " + projectId + " already exists.");
            } else {
              System.out.print(e.getStatusCode().getCode());
              System.out.print(e.isRetryable());
            }
        }
        
    }
}
