package com.hedera.hcsrelay.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TopicTest {
    @Test
    public void gettersAndSetters() throws Exception {
        Topic topic = new Topic();
        topic.setTopic("topic");
        assertAll(
                () -> assertEquals("topic", topic.getTopic())
             );
    }
}
