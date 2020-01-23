package com.hedera.hcs.sxc.relay.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.relay.config.Config;
import com.hedera.hcs.sxc.relay.config.Queue;
import com.hedera.hcs.sxc.relay.config.YAMLConfig;

public class ConfigTest extends AbstractConfigTest {

    @Test
    public void LoadConfig() throws Exception {
        Config config = new Config();
        YAMLConfig yamlConfig = config.getConfig();
        Queue queue = yamlConfig.getQueue();

        assertAll(
                () -> assertEquals("35.222.103.151:6551", yamlConfig.getMirrorAddress())
                ,() -> assertEquals(2, yamlConfig.getTopics().size())
                ,() -> assertEquals("0.0.10", yamlConfig.getTopics().get(0).getTopic())
                ,() -> assertEquals("0.0.11", yamlConfig.getTopics().get(1).getTopic())
                ,() -> assertTopicId(0, 0, 10, yamlConfig.getTopicIds().get(0))
                ,() -> assertTopicId(0, 0, 11, yamlConfig.getTopicIds().get(1))
                ,() -> assertEquals("org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory", queue.getInitialContextFactory())
                ,() -> assertEquals("tcp://hcs-sxc-java-queue:61616", queue.getTcpConnectionFactory())
             );
    }
}
