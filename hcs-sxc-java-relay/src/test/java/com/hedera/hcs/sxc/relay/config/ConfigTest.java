package com.hedera.hcs.sxc.relay.config;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.relay.config.Config;
import com.hedera.hcs.sxc.relay.config.Queue;
import com.hedera.hcs.sxc.relay.config.YAMLConfig;

public class ConfigTest extends AbstractConfigTest {

    @Test
    public void LoadConfig() throws Exception {
        Config config = new Config("./src/test/resources/relay-config.yaml");
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
