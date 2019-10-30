package com.hedera.hcsrelay.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class YAMLConfigTest {
    @Test
    public void gettersAndSetters() throws Exception {
        YAMLConfig yamlConfig = new YAMLConfig();
        List<Topic> topics = new ArrayList<Topic>();
        Queue queue = new Queue();
        
        Topic topic = new Topic();
        topic.setTopic("0.0.10");
        topics.add(topic);
        
        queue.setInitialContextFactory("contextFactory");
        
        yamlConfig.setMirrorAddress("mirrorAddress");
        yamlConfig.setTopics(topics);
        yamlConfig.setQueue(queue);
        
        assertAll(
                () -> assertEquals("mirrorAddress", yamlConfig.getMirrorAddress())
                ,() -> assertEquals(topics, yamlConfig.getTopics())
                ,() -> assertEquals(queue.getInitialContextFactory(), yamlConfig.getQueue().getInitialContextFactory())
                ,() -> assertEquals(1, yamlConfig.getTopicIds().size())
             );
    }

}