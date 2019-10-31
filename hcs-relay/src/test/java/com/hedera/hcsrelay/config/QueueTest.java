package com.hedera.hcsrelay.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class QueueTest {
    @Test
    public void gettersAndSetters() throws Exception {
        Queue queue = new Queue();
        queue.setInitialContextFactory("contextFactory");
        queue.setJGroupsConnectionFactory("jGroupsConnectionFactory");
        queue.setTcpConnectionFactory("tcpConnectionFactory");
        queue.setTopic("topic");
        queue.setVmConnectionFactory("vmConnectionFactory");
        assertAll(
                () -> assertEquals("contextFactory", queue.getInitialContextFactory())
                ,() -> assertEquals("jGroupsConnectionFactory", queue.getJGroupsConnectionFactory())
                ,() -> assertEquals("tcpConnectionFactory", queue.getTcpConnectionFactory())
                ,() -> assertEquals("topic", queue.getTopic())
                ,() -> assertEquals("vmConnectionFactory", queue.getVmConnectionFactory())
             );
    }

}
