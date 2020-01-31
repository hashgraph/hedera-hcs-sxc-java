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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.relay.config.Queue;
import com.hedera.hcs.sxc.relay.config.Topic;
import com.hedera.hcs.sxc.relay.config.YAMLConfig;

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
